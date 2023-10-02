/************************************************************************************
 * Compilers Assignment 5
 *
 * Jude Gabriel
 * April 9, 2023
 *
 * ENHANCEMENTS:
 * Extension 1:
 * 	Produces VTables
 *
 * Extension 2:
 * 	Properly generates string literals
 *
 * Extension 3:
 *  Properly implements switch
 *
 * ***********************************************************************************/

package visitor;

import syntaxtree.*;

import java.util.*;

import errorMsg.*;
import java.io.*;
import java.awt.Point;

//the purpose here is to annotate things with their offsets:
// - formal parameters, with respect to the (callee) frame
// - instance variables, with respect to their slot in the object
// - methods, with respect to their slot in the v-table
public class CG1Visitor extends ASTvisitor {
	
	// IO stream to which we will emit code
	CodeStream code;
	
	// offset in object of next "object" instance variable we encounter
	private int currentObjInstVarOffset;
	
	// offset in object of next "data" instance variable we encounter
	private int currentDataInstVarOffset;
	
	////////////////////////////////////////////////////////////
	// UNCOMMENT THE THREE DECLARATIONS BELOW ONLY IF YOU ARE DOING
	// YOUR OWN VTABLE GENERATION
	////////////////////////////////////////////////////////////
//	// stack method tables for current class and all superclasses
	private Stack<ArrayList<String>> superclassMethodTables;
//	// current method table
	private ArrayList<String> currentMethodTable;
//	// to collect the array types that are referenced in the code
	private HashSet<ArrayType> arrayTypesInCode;

	public CG1Visitor(ErrorMsg e, PrintStream out) {
		initInstanceVars(e, out);
	}
	
	private void initInstanceVars(ErrorMsg e, PrintStream out) {
		currentObjInstVarOffset = 0;
		currentDataInstVarOffset = 0;
		code = new CodeStream(out, e);

		// UNCOMMENT THESE THREE LINES IF YOU ARE DOING YOUR OWN VTABLE
		// GENERATION
		superclassMethodTables = new Stack<ArrayList<String>>();
		superclassMethodTables.addElement(new ArrayList<String>());
		arrayTypesInCode = new HashSet<ArrayType>();
	}
	
	
	////// HELPER METHODS FOR GENERATING ASM-LEVEL SYMBOLS //////
	
	public static String vtableNameFor(Type t) {
		if (t instanceof ArrayType) {
			return "_ARRAY_" + vtableNameFor(((ArrayType)t).baseType);
		}
		else if (t instanceof IdentifierType) {
			return t.toString2();
		}
		else if (t instanceof IntegerType) {
			return "_INT";
		}
		else if (t instanceof BooleanType) {
			return "_BOOLEAN";
		}
		else if (t instanceof VoidType) {
			return "_VOID";
		}
		else if (t instanceof NullType) {
			return "_NULLTYPE";
		}
		else return "_UNKNOWNTYPE";
	}
	
	public static String printStringNameFor(Type t) {
		return printStringNameFor(t, "", "");
	}
	
	public static String printStringNameFor(Type t, String prefix, String suffix) {
		if (t instanceof ArrayType) {
			return "[" + printStringNameFor(((ArrayType)t).baseType,"L",";");
		}
		else if (t instanceof IdentifierType) {
			return printStringNameFor(((IdentifierType)t).link, prefix, suffix);
		}
		else if (t instanceof IntegerType) {
			return "I";
		}
		else if (t instanceof BooleanType) {
			return "Z";
		}
		else return "?";
	}
	
	public static String printStringNameFor(ClassDecl cd) {
		return printStringNameFor(cd, "", "");
	}
	
	public static String printStringNameFor(ClassDecl cd, String prefix, String suffix) {
		String name = cd.name;
		if (name.equals("String") || name.equals("Object")) {
			name = "java.lang." + name;
		}
		return prefix + name + suffix;
	}
	
	public static void emitPrintStringNameFor(ClassDecl cd, CodeStream code) {
		IdentifierType temp = new IdentifierType(cd.pos, cd.name);
		temp.link = cd;
		emitPrintStringNameFor(temp, code);
	}

	public static void emitPrintStringNameFor(Type t, CodeStream code) {
		String printString = CG1Visitor.printStringNameFor(t,"","");
		for (int i = (printString.length()+3)%4; i < 3; i++) {
			code.emit(t, " .byte 0"); // padding
		}

		for (int i = 0; i < printString.length(); i++) {
			char ch = printString.charAt(i);
			int val = (int)ch;
			if (i == 0) {
				code.emit(t, " .byte "+ (val | 0x80) + " # '"+ch+"' with high bit set");
			}
			else {
				code.emit(t, " .byte "+val+ " # '"+ch+"'");
			}
		}
	}

	public static void emitArrayTypeVtables(HashSet<ArrayType> arrayTypesInCode,
			ArrayList<String> objectMethodTable, CodeStream code) {
		
		// array-lists for object base types ([0]) and non-object base
		// types ([1]); this ensures that the data-array objects are
		// emitted last
		ArrayList<ArrayType>[] arrayTypes = new ArrayList[2];
		arrayTypes[0] = new ArrayList<ArrayType>();
		arrayTypes[1] = new ArrayList<ArrayType>();
		for (ArrayType at : arrayTypesInCode) {
//			int idx = isDataType(at.baseType) ? 1 : 0;
			int idx = Sem4Visitor.isObjectType(at.baseType) ? 0 : 1;
			arrayTypes[idx].add(at);
		}
		for (int j = 0; j < 2; j++) {
			// emit label to separate array-of-object and array-of-data vtables
			if (j == 1) {
				code.emit(new IntegerType(-1), "dataArrayVTableStart:");
			}
			for (ArrayType at : arrayTypes[j]) {
				emitPrintStringNameFor(at, code);
				code.emit(at, "CLASS_"+CG1Visitor.vtableNameFor(at)+":");
				for (int i = 0; i < objectMethodTable.size(); i++) {
					code.emit(at, " .word "+objectMethodTable.get(i));
				}
				code.emit(at, "END_CLASS_"+CG1Visitor.vtableNameFor(at)+":");
			}
		}
	}


	/********** HELPER METHODS ***************/
	public boolean isDataType(Type t){
		if(t instanceof BooleanType || t instanceof IntegerType){
			return true;
		}
		return false;
	}

	public boolean isObjectType(Type t){
		if(t instanceof IntegerType || t instanceof BooleanType || t instanceof VoidType){
			return false;
		}
		return true;
	}

	public void registerMethodInTable(MethodDecl n){
		String methodString = "";

		// Check if the method is library or user defined
		if(n.pos < 0){
			methodString = n.name + "_" + n.classDecl.name;
		}
		else{
			methodString = "fcn_" + n.uniqueId + "_" + n.name;
		}

		// Check if it overrides a function
		if(n.superMethod != null){
			String superMethod = "fcn_" + n.superMethod.uniqueId + "_" + n.name;
			if(n.superMethod.pos < 0){
				superMethod = n.superMethod.name + "_" + n.superMethod.classDecl.name;
			}
			for(int i = 0; i < currentMethodTable.size(); i++){
				if(currentMethodTable.get(i).equals(superMethod)){
					currentMethodTable.set(i, methodString);
				}
			}
		}
		else{
			currentMethodTable.add(currentMethodTable.size(), methodString);
		}
	}



	/********** VISITOR METHODS *************/
	public Object visitProgram(Program p) {
		// emit .data
		code.emit(p, " .data");

		// Traverse to the Object class decl and accept it
		ClassDecl superClass = p.classDecls.elementAt(0);
		while(superClass.superLink != null){
			superClass = superClass.superLink;
		}
		superClass.accept(this);

		// flush the output and return
		code.flush();
		return null;
	}

	@Override
	public Object visitClassDecl(ClassDecl n){
		// Set the currentMethodTable to be a copy of the method table for the superclass
		currentMethodTable = (ArrayList<String>) superclassMethodTables.peek().clone();
		code.emit(n, "# ****** class " + n.name + " ******");

		// Set offset variables
		if(n.superLink != null){
			currentDataInstVarOffset = -16 - 4 * n.superLink.numDataInstVars;
			currentObjInstVarOffset = 4 * n.superLink.numObjInstVars;
		}
		else{
			currentDataInstVarOffset = -16;
			currentObjInstVarOffset = 0;
		}

		// Do the traversal the superclass would've done
		super.visitClassDecl(n);

		// Set instvars
		n.numDataInstVars = (-16 - currentDataInstVarOffset) / 4;
		n.numObjInstVars = currentObjInstVarOffset / 4;

		// Call emitPrintStringNameFro to emit the class' print string
		emitPrintStringNameFor(n, code);

		// Emit CLASS_XXX:
		code.emit(n, "CLASS_" + n.name + ":");

		// Loop through the method table to emit each methoid address
		for(int i = 0; i < currentMethodTable.size(); i++){
			// Emit .word ZZZ where ZZZ is the element in the array list
			code.emit(n, " .word " + currentMethodTable.get(i));
		}

		// Push the currentMethodTable onto the superClassMethodTables stack
		superclassMethodTables.push(currentMethodTable);

		// Visit all subclasses
		n.subclasses.accept(this);

		// Call emitArrayTypeVtables for Object class
		if(n.name.equals("Object")){
			emitArrayTypeVtables(arrayTypesInCode, currentMethodTable, code);
		}

		// Pop an element off the superclassMethodTables stack
		superclassMethodTables.pop();

		// Emit end class
		code.emit(n, "END_CLASS_" + n.name + ":");

		return null;
	}

	@Override
	public Object visitMethodDecl(MethodDecl n){

		// Calculate the thisPtrOffset
		int formalParmWords = 0;
		for(int i = 0; i < n.formals.size(); i++){
			if(n.formals.get(i).type instanceof IntegerType){
				formalParmWords += 2;
			}
			else if(!(n.formals.get(i).type instanceof VoidType)){
				formalParmWords += 1;
			}
		}
		n.thisPtrOffset = 4 * (1 + formalParmWords);

		// Do the traversal the superclass would've done
		super.visitMethodDecl(n);

		// Set the vtable offset
		if(n.superMethod != null){
			n.vtableOffset = n.superMethod.vtableOffset;
		}
		else{
			n.vtableOffset = currentMethodTable.size();
		}

		// Register the methods label in hte method table
		registerMethodInTable(n);

		return null;
	}

	@Override
	public Object visitInstVarDecl(InstVarDecl n){
		// Do the visit that the super class would've done
		super.visitInstVarDecl(n);

		// Set the instVarDecl's offest
		if(isDataType(n.type)){
			n.offset = currentDataInstVarOffset;
			currentDataInstVarOffset = currentDataInstVarOffset - 4;
		}
		else if(isObjectType(n.type)){
			n.offset = currentObjInstVarOffset;
			currentObjInstVarOffset = currentObjInstVarOffset + 4;
		}
		return null;
	}

	@Override
	public Object visitArrayType(ArrayType n){
		// Add the type to the arrayTypesInCode Set
		arrayTypesInCode.add(n);
		return null;
	}

	@Override
	public Object visitNewArray(NewArray n){
		// Add the expression to the arrayTypesInCode set
		arrayTypesInCode.add((ArrayType) n.type);
		return null;
	}
}
	
