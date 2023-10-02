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
import errorMsg.*;

import javax.management.ObjectName;
import javax.swing.*;
import java.io.*;
import java.util.List;
import java.util.Locale;

public class CG3Visitor extends ASTvisitor {

	// the purpose here is to annotate things with their offsets:
	// - formal parameters, with respect to the (callee) frame
	// - local variables, with respect to the frame
	// - instance variables, with respect to their slot in the object
	// - methods, with respect to their slot in the v-table
	// - while statements, with respect to the stack-size at the time
	//   of loop-exit
	
	// IO stream to which we will emit code
	CodeStream code;

	// current stack height
	int stackHeight;
	
	// for constant evaluation
	ConstEvalVisitor conEvalVis;
	
	public CG3Visitor(ErrorMsg e, PrintStream out) {
		initInstanceVars(e, out);
		conEvalVis = new ConstEvalVisitor();
	}
	
	@Override
	public Object visitProgram(Program n) {
		// Generate code for the assemblers main program
		code.emit(n, " .text");
		code.indent(n);
		code.emit(n, " .globl main");
		code.emit(n, "main:");
		//code.emit(n, "# initialize registers, etc.");
		code.emit(n, " jal vm_init");

		// Set the visitors stack height to 0
		stackHeight = 0;

		// Generate code for the main object
		n.mainStatement.accept(this);

		//code.emit(n, "# exit program");
		code.emit(n,  " li $v0,10"); // syscall code for "exit program"
		code.emit(n,  " syscall");
		code.unindent(n);


		// emit code for all the methods in all the class declarations
		n.classDecls.accept(this);
		
		// flush the output and return
		code.flush();
		return null;
	}

	@Override
	public Object visitClassDecl(ClassDecl n){
		code.indent(n);
		code.emit(n, "# **** class " + n.name + " ****");
		super.visitClassDecl(n);
		code.unindent(n);
		return null;
	}

	@Override
	public Object visitMethodDeclNonVoid(MethodDeclNonVoid n) {
		code.indent(n);

		// Iterate through formals list. Set offset to be its stack offset
		int counter = n.thisPtrOffset;
		for(int i = 0; i < n.formals.size(); i++){
			if(n.formals.get(i).type instanceof IntegerType){
				counter -= 8;
				n.formals.get(i).offset = counter;
			}
			else if(n.formals.get(i).type instanceof BooleanType || n.formals.get(i).type instanceof ArrayType ||
					n.formals.get(i).type instanceof IdentifierType || n.formals.get(i).type instanceof NullType){
				counter -= 4;
				n.formals.get(i).offset = counter;
			}
			else{
				counter -= 0;
				n.formals.get(i).offset = counter;
			}
		}

		// Emit code
		code.emit(n, ".globl fcn_" + n.uniqueId + "_" + n.name);
		code.emit(n, "fcn_" + n.uniqueId + "_" + n.name + ":");
		code.emit(n, " subu $sp,$sp,4");
		code.emit(n, " sw $s2,($sp) #**\"old this-ptr\"");
		code.emit(n, " lw $s2,"+ n.thisPtrOffset + "($sp) #**\"this-ptr\"");
		code.emit(n, " sw $ra,"+ n.thisPtrOffset + "($sp) #**\"RA:" + n.name + "\"");

		// Set the stack height to 0
		stackHeight = 0;

		// Generate code for the methods body
		for(int i = 0; i < n.stmts.size(); i++){
			n.stmts.get(i).accept(this);
		}

		// Generate code for the methods return expression
		n.rtnExp.accept(this);

		// Determine the saved return address offset and saved this pointer offset
		int RRR = stackHeight + n.thisPtrOffset;
		int PPP = stackHeight;

		// Emit code
		code.emit(n, " lw $ra," + RRR + "($sp)");
		code.emit(n, " lw $s2," + PPP + "($sp)");

		// Determine the offset of the return value DDD
		int DDD = (n.rtnType instanceof IntegerType) ? stackHeight + n.thisPtrOffset - 4 : stackHeight + n.thisPtrOffset;

		// Emit code
		code.emit(n, " lw $t0,($sp)");
		code.emit(n, " sw $t0," + DDD + "($sp)#**\"(return-val)\"");

		// Determine GC tag offset if return is integer type
		if(n.rtnType instanceof IntegerType){
			int GGG = stackHeight + n.thisPtrOffset;
			code.emit(n, " sw $s5,"+ GGG +"($sp)#**\"\"");
		}

		// Determine space for parameters
		int formalsOffset = 0;
		for(int i = 0; i < n.formals.size(); i++){
			if(n.formals.get(i).type instanceof IntegerType){
				formalsOffset += 8;
			}
			else if(!(n.formals.get(i).type instanceof VoidType)){
				formalsOffset += 4;
			}
		}

		// Determine amount to popstack
		int popStack = stackHeight + 4 + formalsOffset + 4;
		if(n.rtnType instanceof IntegerType){
			popStack -= 8;
		}
		else{
			popStack -= 4;
		}

		// Emit code
		code.emit(n, " addu $sp,$sp," + popStack);
		stackHeight -= popStack;
		code.emit(n, "jr $ra");

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitMethodDeclVoid(MethodDeclVoid n) {
		code.indent(n);

		// Iterate through formals list. Set offset to be its stack offset
		int counter = n.thisPtrOffset;
		for(int i = 0; i < n.formals.size(); i++){
			if(n.formals.get(i).type instanceof IntegerType){
				counter -= 8;
				n.formals.get(i).offset = counter;
			}
			else if(n.formals.get(i).type instanceof BooleanType || n.formals.get(i).type instanceof ArrayType ||
					n.formals.get(i).type instanceof IdentifierType || n.formals.get(i).type instanceof NullType){
				counter -= 4;
				n.formals.get(i).offset = counter;
			}
			else{
				counter -= 0;
				n.formals.get(i).offset = counter;
			}
		}

		// Emit code
		code.emit(n, ".globl fcn_" + n.uniqueId + "_" + n.name);
		code.emit(n, "fcn_" + n.uniqueId + "_" + n.name + ":");
		code.emit(n, " subu $sp,$sp,4");
		code.emit(n, " sw $s2,($sp) #**\"old this-ptr\"");
		code.emit(n, " lw $s2,"+ n.thisPtrOffset + "($sp) #**\"this-ptr\"");
		code.emit(n, " sw $ra,"+ n.thisPtrOffset + "($sp) #**\"RA:" + n.name + "\"");

		// Set the visitors stack height to zero
		stackHeight = 0;

		// Generate code for the methods body
		for(int i = 0; i < n.stmts.size(); i++){
			n.stmts.get(i).accept(this);
		}

		code.emit(n," # stack height is " + stackHeight);

		// Determine saved return address offset and saved this pointer offset
		int RRR = stackHeight + n.thisPtrOffset;
		int PPP = stackHeight;

		// Emit code
		code.emit(n, " lw $ra," + RRR + "($sp)");
		code.emit(n, " lw $s2," + PPP + "($sp)");

		// Determine space for parameters
		int formalsOffset = 0;
		for(int i = 0; i < n.formals.size(); i++){
			if(n.formals.get(i).type instanceof IntegerType){
				formalsOffset += 8;
			}
			else if(!(n.formals.get(i).type instanceof VoidType)){
				formalsOffset += 4;
			}
		}

		// Determine the amount to pop the stack
		int popStack = stackHeight + 4 + formalsOffset + 4;


		// Emit code
		code.emit(n, " addu $sp,$sp," + popStack);
		stackHeight -= popStack;
		code.emit(n, "jr $ra");

		code.unindent(n);
		return null;
	}
	
	private void initInstanceVars(ErrorMsg errorMsg, PrintStream out) {
		code = new CodeStream(out, errorMsg);
		code.setVisitor3(this);
		stackHeight = 0;
	}

	@Override
	public Object visitIntegerLiteral(IntegerLiteral n){
		code.indent(n);
		code.emit(n, " subu $sp,$sp,8");
		stackHeight += 8;
		code.emit(n, " sw $s5,4($sp)#**\"GC tag\"");
		code.emit(n, " li $t0," + n.val);
		code.emit(n, " sw $t0,($sp)");
		code.unindent(n);
		return null;
	}

	@Override
	public Object visitNull(Null n){
		code.indent(n);
		code.emit(n, " subu $sp,$sp,4");
		stackHeight += 4;
		code.emit(n, " sw $zero,($sp)#**\"\"");
		code.unindent(n);
		return null;
	}

	@Override
	public Object visitTrue(True n){
		code.indent(n);
		code.emit(n, " subu $sp,$sp,4");
		stackHeight += 4;
		code.emit(n, " li $t0,1");
		code.emit(n, " sw $t0,($sp)#**\"\"");
		code.unindent(n);
		return null;
	}

	@Override
	public Object visitFalse(False n){
		code.indent(n);
		code.emit(n, " subu $sp,$sp,4");
		stackHeight += 4;
		code.emit(n, " sw $zero,($sp)#**\"\"");
		code.unindent(n);
		return null;
	}

	@Override
	public Object visitStringLiteral(StringLiteral n){
		code.indent(n);
		code.emit(n, " subu $sp,$sp,4");
		stackHeight += 4;

		code.emit(n, " la $t0,strLit_" + n.uniqueCgRep.uniqueId);
		String printStr = "";
		for(int i = 0; i < n.str.length(); i++){
			if(n.str.charAt(i) == '\n'){
				printStr = printStr.concat("\\?");
			}
			else{
				printStr = printStr.concat("" + n.str.charAt(i));
			}
		}
		code.emit(n, " sw $t0,($sp)#**\"strLit:" + printStr + "\"");
		code.unindent(n);
		return null;
	}

	@Override
	public Object visitThis(This n){
		code.indent(n);
		code.emit(n, " subu $sp,$sp,4");
		stackHeight += 4;
		code.emit(n, " sw $s2,($sp)#**\"\"");
		code.unindent(n);
		return null;
	}

	@Override
	public Object visitSuper(Super n){
		code.indent(n);
		code.emit(n, " subu $sp,$sp,4");
		stackHeight += 4;
		code.emit(n, " sw $s2,($sp)#**\"\"");
		code.unindent(n);
		return null;
	}

	@Override
	public Object visitIdentifierExp(IdentifierExp n){
		code.indent(n);
		if(n.link instanceof InstVarDecl){
			// Determine variables offset
			code.emit(n, " lw $t0,"+ n.link.offset +"($s2) # inst-var");
		}
		else if(n.link instanceof LocalVarDecl || n.link instanceof FormalDecl){
			// Compute stack depth
			int stackDepth = stackHeight + n.link.offset;
			code.emit(n, " lw $t0,"+ stackDepth +"($sp) # local: stackHt=" + stackHeight + ",var-offset=" + n.link.offset);
		}

		if(n.type instanceof IntegerType){
			code.emit(n, " subu $sp,$sp,8");
			stackHeight += 8;
			code.emit(n, " sw $s5,4($sp)#**\"GC tag\"");
			code.emit(n, " sw $t0,($sp)#**\"\"");
		}
		else if(!(n.type instanceof VoidType)){
			code.emit(n, " subu $sp,$sp,4");
			stackHeight += 4;
			code.emit(n, " sw $t0,($sp)#**\"\"");
		}

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitNot(Not n){
		code.indent(n);

		// Generate code for the subexpressions
		n.exp.accept(this);

		//Emit code
		code.emit(n, " lw $t0,($sp)");
		code.emit(n, " xor $t0,$t0,1");
		code.emit(n, " sw $t0,($sp)#**\"\"");

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitPlus(Plus n){
		code.indent(n);
		n.left.accept(this);
		n.right.accept(this);
		code.emit(n, " lw $t0,($sp)");
		code.emit(n, " lw $t1,8($sp)");
		code.emit(n, " addu $t0,$t0,$t1");
		code.emit(n, " addu $sp,$sp,8");
		code.emit(n, " sw $t0,($sp)#**\"\"");
		stackHeight -= 8;
		code.unindent(n);
		return null;
	}

	@Override
	public Object visitMinus(Minus n){
		code.indent(n);
		n.left.accept(this);
		n.right.accept(this);
		code.emit(n, " lw $t0,($sp)");
		code.emit(n, " lw $t1,8($sp)");
		code.emit(n, " subu $t0,$t1,$t0");
		code.emit(n, " addu $sp,$sp,8");
		stackHeight -= 8;
		code.emit(n, " sw $t0,($sp)#**\"\"");
		code.unindent(n);
		return null;
	}

	@Override
	public Object visitTimes(Times n){
		code.indent(n);

		// Generate code for the left and right subexpressions
		n.left.accept(this);
		n.right.accept(this);

		//Emit code
		code.emit(n, " lw $t0,($sp)");
		code.emit(n, " lw $t1,8($sp)");
		code.emit(n, " mult $t0,$t1");
		code.emit(n, " mflo $t0");
		code.emit(n, " addu $sp,$sp,8");
		code.emit(n, " sw $t0,($sp)#**\"\"");
        stackHeight -= 8;

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitDivide(Divide n){
		// Generate code for left and right subexpressions
		n.left.accept(this);
		n.right.accept(this);

		// Emit code
		code.emit(n, " jal divide");
		stackHeight -= 8;

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitRemainder(Remainder n){
		code.indent(n);

		// Generate code for left and right subexpressions
		n.left.accept(this);
		n.right.accept(this);

		// Emit code
		code.emit(n, " jal remainder");
		stackHeight -= 8;

		code.unindent(n);
		return null;
	}


	@Override
	public Object visitEquals(Equals n){
		code.indent(n);

		// Generate code for left and right subexpressions
		n.left.accept(this);
		n.right.accept(this);

		// Emit code
		if(n.left.type instanceof IntegerType && n.right.type instanceof IntegerType){
			code.emit(n, " lw $t0,($sp)");
			code.emit(n, " lw $t1,8($sp)");
			code.emit(n, " seq $t0,$t0,$t1");
			code.emit(n, " addu $sp,$sp,12");
			stackHeight -= 12;
			code.emit(n, " sw $t0,($sp)#**\"\"");
		}
		else{
			code.emit(n, " lw $t0,($sp)");
			code.emit(n, " lw $t1,4($sp)");
			code.emit(n, " seq $t0,$t0,$t1");
			code.emit(n, " addu $sp,$sp,4");
			stackHeight -= 4;
			code.emit(n, " sw $t0,($sp)#**\"\"");
		}

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitGreaterThan(GreaterThan n){
		code.indent(n);

		// Generate code for left and right subexpressions
		n.left.accept(this);
		n.right.accept(this);

		// Generate code
		code.emit(n, " lw $t0,($sp)");
		code.emit(n, " lw $t1,8($sp)");
		code.emit(n, " sgt $t0,$t1,$t0");
		code.emit(n, " addu $sp,$sp,12");
		stackHeight -= 12;
		code.emit(n, " sw $t0,($sp)#**\"\"");

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitLessThan(LessThan n){
		code.indent(n);

		// Generate code for left and right subexpressions
		n.left.accept(this);
		n.right.accept(this);

		// Generate code
		code.emit(n, " lw $t0,($sp)");
		code.emit(n, " lw $t1,8($sp)");
		code.emit(n, " slt $t0,$t1,$t0");
		code.emit(n, " addu $sp,$sp,12");
		stackHeight -= 12;
		code.emit(n, " sw $t0,($sp)#**\"\"");

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitAnd(And n){
		code.indent(n);

		// Generate code for the left subexpression
		n.left.accept(this);

		// Emit code
		code.emit(n, " lw $t0,($sp)");
		code.emit(n, " beq $t0,$zero, skip_" + n.uniqueId);
		code.emit(n, " addu $sp,$sp,4");
		stackHeight -= 4;

		// Generate code for right subexpression
		n.right.accept(this);

		// Emit code
		code.emit(n, "skip_" + n.uniqueId + ":");

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitOr(Or n){
		code.indent(n);

		// Generate code for the left subexpression
		n.left.accept(this);

		// Emit code
		code.emit(n, " lw $t0,($sp)");
		code.emit(n, " bne $t0,$zero, skip_" + n.uniqueId);
		code.emit(n, " addu $sp,$sp,4");
		stackHeight -= 4;

		// Generate code for right subexpression
		n.right.accept(this);

		// Emit code
		code.emit(n, "skip_" + n.uniqueId + ":");

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitArrayLength(ArrayLength n){
		code.indent(n);

		// Generate code for subexpression
		n.exp.accept(this);

		// Emit code
		code.emit(n, " lw $t0,($sp)");
		code.emit(n, " beq $t0,$zero,nullPtrException");
		code.emit(n, " lw $t0,-4($t0)");
		code.emit(n, " sw $s5,($sp)#**\"GC tag\"");
		code.emit(n, " subu $sp,4");
		code.emit(n, " sw $t0,($sp)#**\"\"");
		stackHeight += 4;

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitArrayLookup(ArrayLookup n){
		// Generate code for the arrExp expression
		n.arrExp.accept(this);

		// Generate code for the idxExp expression
		n.idxExp.accept(this);

		// Emit code
		code.emit(n, " lw $t0,8($sp)");
		code.emit(n, " beq $t0,$zero,nullPtrException");
		code.emit(n, " lw $t1,-4($t0)");
		code.emit(n, " lw $t2,($sp)");
		code.emit(n, " bgeu $t2,$t1,arrayIndexOutOfBounds");
		code.emit(n, " sll $t2,$t2,2");
		code.emit(n, " addu $t2,$t2,$t0");
		code.emit(n, " lw $t0,($t2)");

		if(n.type instanceof IntegerType){
			code.emit(n, " sw $t0,4($sp)");
			code.emit(n, " sw $s5,8($sp)#**\"\"");
			code.emit(n, " addu $sp,$sp,4");
			stackHeight -= 4;
		}
		else if(!(n.type instanceof VoidType)){
			code.emit(n, " sw $t0,8($sp)#**\"\"");
			code.emit(n, " addu $sp,$sp,8");
			stackHeight -= 8;
		}

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitInstVarAccess(InstVarAccess n){
		code.indent(n);

		// Generate code for the subexpression
		n.exp.accept(this);

		// Emit code
		code.emit(n, " lw $t0,($sp)");
		code.emit(n, " beq $t0,$zero,nullPtrException");
		code.emit(n, " lw $t0," + n.varDec.offset + "($t0)");

		if(n.type instanceof IntegerType){
			code.emit(n, " subu $sp,$sp,4");
			code.emit(n, " sw $s5,4($sp)#**\"GC tag\"");
			code.emit(n, " sw $t0,($sp)#**\"\"");
			stackHeight += 4;
		}
		else if(!(n.type instanceof VoidType)){
			code.emit(n, " sw $t0,($sp)#**\"\"");
		}

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitInstanceOf(InstanceOf n){
		code.indent(n);

		// Generate code for the subexpression
		n.exp.accept(this);

		// Emit the code
		code.emit(n, " la $t0,CLASS_" + CG1Visitor.vtableNameFor(n.checkType));
		code.emit(n, " la $t1,END_CLASS_" + CG1Visitor.vtableNameFor(n.checkType));
		code.emit(n, " jal instanceOf");

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitCast(Cast n){
		code.indent(n);

		// Generate code for the subexpression
		n.exp.accept(this);

		// Emit code
		code.emit(n, " la $t0,CLASS_" + CG1Visitor.vtableNameFor(n.type));
		code.emit(n, " la $t1,END_CLASS_" + CG1Visitor.vtableNameFor(n.type));
		code.emit(n, " jal checkCast");

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitNewObject(NewObject n){
		code.indent(n);

		// Determine the number of object and data instance variables
		int numObjVars = n.objType.link.numObjInstVars;
		int numDataVars = n.objType.link.numDataInstVars + 1;

		// Emit code
		code.emit(n, " li $s6," + numDataVars);
		code.emit(n, " li $s7," + numObjVars);
		code.emit(n, " jal newObject");
		stackHeight = stackHeight + 4;
		code.emit(n, " la $t0,CLASS_" + n.objType.link.name);
		code.emit(n, " sw $t0,-12($s7)#**\"VTP: " + n.objType.link.name + "\"");

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitNewArray(NewArray n){
		code.indent(n);

		// Generate code for the array-size expression
		n.sizeExp.accept(this);

		// Emit Code
		code.emit(n, " lw $s7,($sp)");
		code.emit(n, " addu $sp,$sp,8");
		stackHeight -= 8;
		code.emit(n, " li $s6, 1");
		code.emit(n, " jal newObject");
		stackHeight += 4;
		code.emit(n, " la $t0,CLASS_" + CG1Visitor.vtableNameFor(n.type));
		code.emit(n, " sw $t0,-12($s7)#**\"VTP: " + CG1Visitor.vtableNameFor(n.type) + "\"");

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitCall(Call n){
		// We will treat is as if it was just super
		code.indent(n);

		if(n.obj instanceof Super){
			// Save current stack height for use later
			int currStackHeight = stackHeight;

			// Generate code for the object expression
			n.obj.accept(this);

			// Generate code for all parameter expressions
			for(int i = 0; i < n.parms.size(); i++){
				n.parms.get(i).accept(this);
			}

			// Handle library methods
			if(n.methodLink.pos < 0){
				code.emit(n, " jal " + n.methName + "_" + n.methodLink.classDecl.name + " # " + n.methName);
			}
			else{
				code.emit(n, " jal fcn_" + n.methodLink.uniqueId + "_" + n.methName);
			}

			// Set the stack height to saved one plus 0, 4, or 8 depending on expressions type
			if(n.type instanceof IntegerType){
				stackHeight = currStackHeight + 8;
			}
			else if(n.type instanceof BooleanType || n.type instanceof ArrayType ||
					n.type instanceof IdentifierType || n.type instanceof NullType){
				stackHeight = currStackHeight + 4;
			}
			else{
				stackHeight = currStackHeight;
			}
		}
		else{
			// Save current stack height
			int currStackHeight = stackHeight;

			// Generate code for the objects expression
			n.obj.accept(this);

			// Generate code for all the parameter expressions
			for(int i = 0; i < n.parms.size(); i++){
				n.parms.get(i).accept(this);
			}

			// Compute methods pointer offset minus 4
			int valOffset = n.methodLink.thisPtrOffset - 4;

			// Compute vtable offset times 4
			int valVTable = 4 * n.methodLink.vtableOffset;

			// Emit code
			code.emit(n, " lw $t0," + valOffset + "($sp)");
			code.emit(n, " beq $t0,$zero,nullPtrException");
			code.emit(n, " lw $t0,-12($t0)");
			code.emit(n, " lw $t0," + valVTable + "($t0)");
			code.emit(n, " jalr $t0 # " + n.methName);

			// Set the stack height to saved one plus 0, 4, or 8 depending on expressions type
			if(n.type instanceof IntegerType){
				stackHeight = currStackHeight + 8;
			}
			else if(n.type instanceof BooleanType || n.type instanceof ArrayType ||
					n.type instanceof IdentifierType || n.type instanceof NullType){
				stackHeight = currStackHeight + 4;
			}
			else{
				stackHeight = currStackHeight;
			}
		}

		code.unindent(n);
		return null;
	}


	@Override
	public Object visitLocalVarDecl(LocalVarDecl n){
		code.indent(n);
		n.initExp.accept(this);
		n.offset = -stackHeight;
		code.unindent(n);
		return null;
	}

	@Override
	public Object visitCallStatement(CallStatement n){
		code.indent(n);

		// Generate code for the expression
		n.callExp.accept(this);

		if(n.callExp.type instanceof IntegerType){
			code.emit(n, "addu $sp,$sp,8");
			stackHeight -= 8;
		}
		else if(!(n.callExp.type instanceof VoidType)){
			code.emit(n, "addu $sp,$sp,4");
			stackHeight -= 4;
		}

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitBlock(Block n){
		code.indent(n);

		// Save current stackHeight for use later
		int stackHeightSaved = stackHeight;

		// Generate code for the blocks statements
		for(int i = 0; i < n.stmts.size(); i++){
			n.stmts.get(i).accept(this);
		}

		// Check if stack heights are different
		if(stackHeightSaved != stackHeight){
			int offset = stackHeight - stackHeightSaved;
			code.emit(n, " addu $sp," + offset);
		}

		// Revert the stackHeight back to the saved value
		stackHeight = stackHeightSaved;

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitIf(If n){
		code.indent(n);

		// Generate code for the test expression
		n.exp.accept(this);

		// Emit code
		code.emit(n, " lw $t0,($sp)");
		code.emit(n, " addu $sp,$sp,4");
		stackHeight -= 4;
		code.emit(n, " beq $t0,$zero,if_else_" + n.uniqueId);

		// Generate code for the true statement
		n.trueStmt.accept(this);

		// Emit Code
		code.emit(n, "j if_done_" + n.uniqueId);
		code.emit(n, "if_else_" + n.uniqueId + ":");

		// Generate code for the false statement
		n.falseStmt.accept(this);

		// Emit code
		code.emit(n, "if_done_" + n.uniqueId + ":");

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitWhile(While n){
		code.indent(n);

		// Set whiles stack height to bne the current stack height
		n.stackHeight = stackHeight;

		// Emit code
		code.emit(n, "j while_enter_" + n.uniqueId);
		code.emit(n, "while_top_" + n.uniqueId + ":");

		// Generate code for the whiles body
		n.body.accept(this);

		// Emit code
		code.emit(n, "while_enter_" + n.uniqueId + ":");

		// Generate code for the whiles test expression
		n.exp.accept(this);

		// Emit code
		code.emit(n, " lw $t0,($sp)");
		code.emit(n, " addu $sp,$sp,4");
		stackHeight -= 4;
		code.emit(n, "bne $t0,$zero,while_top_" + n.uniqueId);
		code.emit(n, "break_target_" + n.uniqueId + ":");

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitBreak(Break n){
		code.indent(n);

		// Compute difference between stack height and enclosing while or switch stack height
		int stackDiff = stackHeight - n.breakLink.stackHeight;

		// Emit code
		if(stackDiff != 0){
			code.emit(n, " addu $sp," + stackDiff);
		}
		code.emit(n, "j break_target_" + n.breakLink.uniqueId);

		code.unindent(n);
		return null;
	}


	@Override
	public Object visitAssign(Assign n){
		code.indent(n);


		if(n.lhs instanceof IdentifierExp){
			// Generate code for the rhs expression
			n.rhs.accept(this);

			// Emit code
			code.emit(n, " lw $t0,($sp)");

			// Check if LS is an instVarDecl
			if(((IdentifierExp) n.lhs).link instanceof InstVarDecl){
				InstVarDecl instVarDecl = (InstVarDecl) ((IdentifierExp) n.lhs).link;
				code.emit(n, " sw $t0," + instVarDecl.offset +"($s2)");
			}
			else{
				int offset = stackHeight + ((IdentifierExp) n.lhs).link.offset;
				code.emit(n, " sw $t0,"+ offset +"($sp)");
			}

			// Check if expression is integer type
			if(n.lhs.type instanceof IntegerType){
				code.emit(n, " addu $sp,$sp,8");
				stackHeight -= 8;
			}
			else if(n.lhs.type instanceof BooleanType || n.lhs.type instanceof ArrayType ||
					n.lhs.type instanceof IdentifierType || n.lhs.type instanceof NullType){
				code.emit(n, " addu $sp,$sp,4");
				stackHeight -= 4;
			}
		}
		else if(n.lhs instanceof InstVarAccess){
			// Generate code for the InstVarAccess' exp expression
			((InstVarAccess) n.lhs).exp.accept(this);

			// Generate code for the rhs expression
			n.rhs.accept(this);

			// Emit code
			code.emit(n, " lw $t0,($sp)");

			// Check if rhs is integer type
			if(n.rhs.type instanceof IntegerType){
				code.emit(n, " lw $t1,8($sp)");
			}
			else if(n.rhs.type instanceof BooleanType || n.rhs.type instanceof ArrayType ||
					n.rhs.type instanceof IdentifierType || n.rhs.type instanceof NullType){
				code.emit(n, " lw $t1,4($sp)");
			}

			// Emit code
			code.emit(n, " beq $t1,$zero,nullPtrException");
			code.emit(n, " sw $t0,"+ ((InstVarAccess) n.lhs).varDec.offset + "($t1)");


			// Check if expression is integer type
			if(n.lhs.type instanceof IntegerType){
				code.emit(n, " addu $sp,$sp,12");
				stackHeight -= 12;
			}
			else if(n.lhs.type instanceof BooleanType || n.lhs.type instanceof ArrayType ||
					n.lhs.type instanceof IdentifierType || n.lhs.type instanceof NullType){
				code.emit(n, " addu $sp,$sp,8");
				stackHeight -= 8;
			}
		}
		else if(n.lhs instanceof ArrayLookup){
			// Generate code for the array expression
			((ArrayLookup) n.lhs).arrExp.accept(this);

			// Generate code for the array index expression
			((ArrayLookup) n.lhs).idxExp.accept(this);

			// Generate code for the RHS expression
			n.rhs.accept(this);

			// Add four if the value being assigned has int type
			int offsetA = 12;
			int offsetB = 4;
			int offsetC = 16;
			if(n.rhs.type instanceof IntegerType){
				offsetA += 4;
				offsetB += 4;
				offsetC +=4;
			}

			// Emit code
			code.emit(n, " lw $t0,($sp)");
			code.emit(n, " lw $t1," + offsetA + "($sp)");
			code.emit(n, " beq $t1,$zero,nullPtrException");
			code.emit(n, " lw $t2," + offsetB + "($sp)");
			code.emit(n, " lw $t3,-4($t1)");
			code.emit(n, " bgeu $t2,$t3,arrayIndexOutOfBounds");
			code.emit(n, " sll $t2,$t2,2");
			code.emit(n, " addu $t2,$t2,$t1");
			code.emit(n, " sw $t0,($t2)");
			code.emit(n, " addu $sp,$sp," + offsetC);
			stackHeight -= offsetC;
		}

		code.unindent(n);
		return null;
	}

	@Override
	public Object visitLabel(Label n){
		// Set the stack height to be the enclosing switch's stack height
		stackHeight = n.enclosingSwitch.stackHeight;

		// Emit code
		code.emit(n, "case_label_" + n.uniqueId + ":");

		return null;
	}

	@Override
	public Object visitSwitch(Switch n){
		code.indent(n);

		// Set the switch's stack height to be the stack height
		n.stackHeight = stackHeight;

		// Generate code for the switch expression
		n.exp.accept(this);

		// Emit code
		code.emit(n, "lw $t0,($sp)");
		code.emit(n, "addu $sp,8");
		stackHeight -= 8;

		// Generate the conditional branching code
		// Declare a variable to contain the default object initially null
		Default holdDefault = null;

		// Iterate through top level statements
		for(int i = 0; i < n.stmts.size(); i++){
			// If it is a Default object, set default variable
			if(n.stmts.get(i) instanceof Default){
				holdDefault = (Default) n.stmts.get(i);
			}

			// Handle cases
			if(n.stmts.get(i) instanceof Case){
				// Constant eval the case expression
				Integer obj = (Integer) ((Case) n.stmts.get(i)).exp.accept(conEvalVis);

				// Emit code
				code.emit(n, "li $t1," + obj);
				code.emit(n, "beq $t0,$t1,case_label_" + n.stmts.get(i).uniqueId);
			}
		}

		// Handle the saved default
		if(holdDefault != null){
			code.emit(n, "j case_label_" + holdDefault.uniqueId);
		}
		else{
			code.emit(n, "j break_target_" + n.uniqueId);
		}

		// Generate code for the switch block
		for(int i = 0; i < n.stmts.size(); i++){
			if(n.stmts.get(i) instanceof Case){
				this.visitLabel((Case) n.stmts.get(i));
			}
			else{
				n.stmts.get(i).accept(this);
			}
		}

		// Emit code
		code.emit(n, "break_target_" + n.uniqueId + ":");

		// Restore stackHeight top be the switch's stack height
		stackHeight = n.stackHeight;

		code.unindent(n);
		return null;
	}
}


	
