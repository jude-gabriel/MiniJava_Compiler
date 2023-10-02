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
// - local variables, with respect to the frame
// - instance variables, with respect to their slot in the object
// - methods, with respect to their slot in the v-table
// - while statements, with respect to the stack-size at the time
//   of loop-exit
public class CG2Visitor extends ASTvisitor {

	// IO stream to which we will emit code
	CodeStream code;

	// hash-table that keeps track of unique representative for
	// each string literal
	Hashtable<String,StringLiteral> stringTable;

	public CG2Visitor(ErrorMsg e, PrintStream out) {
		initInstanceVars(e, out);
	}

	public Object visitProgram(Program p) {
		///// CS 358 STUDENTS:
		///// THIS WILL GENERATE ASM-DIRECTIVES FOR STRING LITERALS THAT DON'T HANDLE
		///// '==' CORRECTLY. REPLACE THIS CALL WITH YOUR OWN CODE IF YOU WANT TO
		///// IMPLEMENT THE "STRING ==" EXTENSION
		//StrLitSimpleGenerator.generate(p,  code);

		// Emit .data
		code.emit(p, "  .data");

		// Traverse the class declerations
		p.classDecls.accept(this);

		// flush the output and return
		code.flush();
		return null;
	}

	private void initInstanceVars(ErrorMsg errorMsg, PrintStream out) {
		stringTable = new Hashtable<String,StringLiteral>();
		code = new CodeStream(out, errorMsg);
	}

	@Override
	public Object visitStringLiteral(StringLiteral n){
		// Lookup the string in the hashtable
		if(stringTable.get(n.str) != null){
			n.uniqueCgRep = stringTable.get(n.str);
		}
		else{
			stringTable.put(n.str, n);
			n.uniqueCgRep = n;
			for(int i = 0; i < n.str.length(); i++){
				code.emit(n, "  .byte " + (int)n.str.charAt(i));
			}
			int byteNums = n.str.length() % 4;
			if(byteNums != 0){
				byteNums = 4 - byteNums;
			}
			for(int i = 0; i < byteNums; i++){
				code.emit(n, "  .byte 0");
			}
			code.emit(n, "  .word CLASS_String");
			int roundedUp = ((n.str.length() + 4 - 1) / 4) + 1;
			code.emit(n, "  .word " + roundedUp);
			if(n.str.length() == 0){
				code.emit(n, "  .word " + n.str.length());
			}
			else{
				code.emit(n, "  .word -" + n.str.length());
			}
			code.emit(n, "strLit_" + n.uniqueId + ":");
		}
		return null;
	}

}
