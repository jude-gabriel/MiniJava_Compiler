package visitor;

import java.io.PrintStream;
import syntaxtree.*;

public class PrettyPrintVisitor extends ASTvisitor {
	
	private boolean printLinks;
	private boolean printTypes;
	private boolean verbose;
	private PrintStream ps;
	private int indent = 0;

	public PrettyPrintVisitor(PrintStream ps) {
		this(false,false,ps);
	}
	public PrettyPrintVisitor(boolean printLinks, PrintStream ps) {
		this(printLinks,false,ps);
	}
	public PrettyPrintVisitor(boolean printLinks, boolean printTypes, PrintStream ps) {
		this(printLinks, printTypes, false, ps);
	}
	public PrettyPrintVisitor(boolean printLinks, boolean printTypes, boolean verbose, PrintStream ps) {
		this.printLinks = printLinks;
		this.printTypes = printTypes;
		this.verbose = verbose;
		this.ps = ps;
	}
	
	protected void binOp(BinExp n, String opString) {
		ps.print("(");
		print(n.left);
		ps.print(opString);
		print(n.right);
		ps.print(")"+typ(n.type));
	}
	
	@Override
	public Object visitAnd(And n) {
		binOp(n, "&&");
		return null;
	}
	
	@Override
	public Object visitArrayLength(ArrayLength n) {
		print(n.exp);
		ps.print(".length"+typ(n.type));
		return null;
	}

	@Override
	public Object visitArrayLookup(ArrayLookup n) {
		print(n.arrExp);
		ps.print("[");
		print(n.idxExp);
		ps.print("]"+typ(n.type));
		return null;
	}

	@Override
	public Object visitArrayType(ArrayType n) {
		print(n.baseType);
		ps.print("[]");
		return null;
	}
	
	@Override
	public Object visitAssign(Assign n) {
		tab();
		print(n.lhs);
		ps.print(" = ");
		print(n.rhs);
		ps.println(";");
		return null;
	}
	
	@Override
	public Object visitBooleanType(BooleanType n) {
		ps.print("boolean");
		return null;
	}
	
	@Override
	public Object visitBlock(Block n) {
		tab();
		ps.println("{");
		indent();
		if (n.stmts == null) {
			tab();
			ps.println("??null??");
		}
		else {
			print(n.stmts);
		}
		unindent();
		tab();
		ps.println("}");
		return null;
	}
	
	@Override
	public Object visitBreak(Break n) {
		tab();
		String xtn = n.breakLink != null && printLinks() ? "@"+n.breakLink.uniqueId : "";
		ps.println("break"+xtn+";");
		return null;
	}
	
	@Override
	public Object visitCallStatement(CallStatement n) {
		tab();
		print(n.callExp);
		ps.println(";");
		return null;
	}
	
	@Override
	public Object visitCall(Call n) {
		String xtn = n.methodLink != null && printLinks() ? "@"+n.methodLink.uniqueId : "";
		print(n.obj);
		ps.print("."+n.methName+xtn+"(");
		String sep = "";
		if (n.parms == null) {
			ps.print("??null??");
		}
		else {
			for (Exp x : n.parms) {
				ps.print(sep);
				sep = ",";
				print(x);
			}

		}
		ps.print(")"+typ(n.type));
		return null;
	}
	
	@Override
	public Object visitCast(Cast n) {
		ps.print("((");
		print(n.castType);
		ps.print(")"+typ(n.type));
		print(n.exp);
		ps.print(")");
		return null;
	}
	
	@Override
	public Object visitCase(Case n) {
		String xtn =
				n.enclosingSwitch != null && printLinks() ? "@"+n.enclosingSwitch.uniqueId : "";
		tab();
		ps.print("case"+xtn+" ");
		print(n.exp);
		ps.println(":");
		return null;
	}
	
	@Override
	public Object visitClassDecl(ClassDecl n) {
		tab();
 		if (printLinks()) {
 			String xtnStr = n.superLink == null ? "" : "@"+n.superLink.uniqueId;
 			ps.println("class "+n.name+"#"+n.uniqueId+" extends "+n.superName+xtnStr+" {");
 		}
 		else {
 			ps.println("class "+n.name+" extends "+n.superName+ " {");
 		}
		indent();
		print(n.decls);
		unindent();
		tab();
		ps.println("}");
		return null;
	}

	@Override
	public Object visitDefault(Default n) {
		tab();
		String xtn = n.enclosingSwitch != null && printLinks() ? "@"+n.enclosingSwitch.uniqueId : ""; 
		ps.println("default"+xtn+":");
		return null;
	}
	
	@Override
	public Object visitDivide(Divide n) {
		binOp(n, "/");
		return null;
	}
	
	@Override
	public Object visitEquals(Equals n) {
		binOp(n, "==");
		return null;
	}
	
	@Override
	public Object visitFalse(False n) {
		ps.print("false"+typ(n.type));
		return null;
	}

	@Override
	public Object visitFormalDecl(FormalDecl n) {
		print(n.type);
		String xtn = printLinks() ? "#"+n.uniqueId : "";
		ps.print(" "+n.name+xtn);
		return null;
	}
	
	@Override
	public Object visitGreaterThan(GreaterThan n) {
		binOp(n, ">");
		return null;
	}
	
	@Override
	public Object visitIdentifierExp(IdentifierExp n) {
		String xtn = n.link != null && printLinks() ? "@"+n.link.uniqueId : "";
		ps.print(n.name+xtn+typ(n.type));
		return null;
	}
	
	@Override
	public Object visitIdentifierType(IdentifierType n) {
 		String xtn = n.link != null && printLinks() ? "@"+n.link.uniqueId : "";
 		ps.print(n.name+xtn);
 		return null;
	}
	
	@Override
	public Object visitIf(If n) {
		tab();
		ps.print("if (");
		print(n.exp);
		ps.println(")");
		indent();
		print(n.trueStmt);
		unindent();
		tab();
		ps.println("else");
		indent();
		print(n.falseStmt);
		unindent();
		return null;
	}
	
	@Override
	public Object visitInstanceOf(InstanceOf n) {
		ps.print("(");
		print(n.exp);
		ps.print(" instanceof "+typ(n.type));
		print(n.checkType);
		ps.print(")");
		return null;
	}
	
	@Override
	public Object visitInstVarAccess(InstVarAccess n) {
		print(n.exp);
 		String xtn = n.varDec != null && printLinks() ? "@"+n.varDec.uniqueId : "";
 		ps.print("."+typ(n.type)+n.varName+xtn);
		return null;
	}
	
	@Override
	public Object visitInstVarDecl(InstVarDecl n) {
		tab();
		print(n.type);
 		String xtn = printLinks() ? "#"+n.uniqueId : "";
 		ps.println(" "+n.name+xtn+";");
		return null;
	}
	
	@Override
	public Object visitIntegerLiteral(IntegerLiteral n) {
		ps.print(n.val+typ(n.type));
		return null;
	}
	
	@Override
	public Object visitIntegerType(IntegerType n) {
		ps.print("int");
		return null;
	}
	
	@Override
	public Object visitLessThan(LessThan n) {
		binOp(n, "<");
		return null;
	}
	
	public Object visitLocalDeclStatement(LocalDeclStatement n) {
		tab();
		print(n.localVarDecl);
		ps.println(";");
		return null;
	}
	
	public Object visitLocalVarDecl(LocalVarDecl n) {
		print(n.type);
 		String xtn = printLinks() ? "#"+n.uniqueId : "";
 		ps.print(" "+n.name+xtn+" = ");
		print(n.initExp);
		return null;
	}
	
	@Override
	public Object visitMethodDeclVoid(MethodDeclVoid n) {
		tab();
 		String xtn = printLinks() ? "#"+n.uniqueId : "";
 		ps.print("public void "+n.name+xtn+"(");
		String sep = "";
		if (n.formals == null) {
			ps.print("??null??");
		}
		else {
			for (VarDecl d : n.formals) {
				ps.print(sep);
				sep = ", ";
				print(d);
			}
		}
		ps.println(") {");
		indent();
		print(n.stmts);
		unindent();
		tab();
		ps.println("}");
		return null;
	}
	
	@Override
	public Object visitMethodDeclNonVoid(MethodDeclNonVoid n) {
		tab();
		ps.print("public ");
		print(n.rtnType);
		String xtn = printLinks() ? "#"+n.uniqueId : "";
		ps.print(" "+n.name+xtn+"(");
		String sep = "";
		if (n.formals == null) {
			ps.print("??null??");
		}
		else {
			for (VarDecl d : n.formals) {
				ps.print(sep);
				sep = ", ";
				print(d);
			}
		}
		ps.println(") {");
		indent();
		print(n.stmts);
		tab();
		ps.print("return ");
		print(n.rtnExp);
		ps.println(";");
		unindent();
		tab();
		ps.println("}");
		return null;
	}
	
	@Override
	public Object visitMinus(Minus n) {
		binOp(n, "-");
		return null;
	}
	
	@Override
	public Object visitNewArray(NewArray n) {
		Type currType = n.objType;
		int count = 0;
		while (currType instanceof ArrayType) {
			currType = ((ArrayType)currType).baseType;
			count++;
		}
		ps.print("new"+" ");
		print(currType);
		ps.print("[");
		print(n.sizeExp);
		ps.print("]");
		for (int i = 0; i < count; i++) {
			ps.print("[]"+typ(n.type));
		}
		return null;
	}
	
	@Override
	public Object visitNewObject(NewObject n) {
		ps.print("new ");
		print(n.objType);
		ps.print("()"+typ(n.type));
		return null;
	}
	
	@Override
	public Object visitNot(Not n) {
		ps.print("(");
		ps.print("!");
		print(n.exp);
		ps.print(")"+typ(n.type));
		return null;
	}
	
	@Override
	public Object visitNull(Null n) {
		ps.print("null"+typ(n.type));
		return null;
	}
	
	@Override
	public Object visitNullType(NullType n) {
		ps.print("#null-type");
		return null;
	}
	
	@Override
	public Object visitOr(Or n) {
		binOp(n, "||");
		return null;
	}
	
	@Override
	public Object visitPlus(Plus n) {
		binOp(n, "+");
		return null;
	}
	
	@Override
	public Object visitProgram(Program n) {
		if (n.classDecls == null) {
			tab();
			ps.println("??null??");
		}
		else {
			print(n.classDecls);
			if (verbose) {
				ps.println("=====================================================================");
				print(n.mainStatement);
				ps.println("=====================================================================");
				print(n.dummyNodes);
			}
		}
		return null;
	}
	
	@Override
	public Object visitRemainder(Remainder n) {
		binOp(n, "%");
		return null;
	}
	
	@Override
	public Object visitStringLiteral(StringLiteral n) {
		ps.print("\"");
		for (int i = 0; i < n.str.length(); i++) {
			char ch = n.str.charAt(i);
			String ss = ""+ch;
			switch (ch) {
			case '\\': ss = "\\\\"; break;
			case '\"': ss = "\\\""; break;
			case '\n': ss = "\\n"; break;
			case '\t': ss = "\\t"; break;
			case '\r': ss = "\\r"; break;
			case '\f': ss = "\\f"; break;
			}
			ps.print(ss);
		}
		ps.print("\""+typ(n.type));
		return null;
	}
	
	@Override
	public Object visitSuper(Super n) {
		ps.print("super"+typ(n.type));
		return null;
	}
	
	@Override
	public Object visitSwitch(Switch n) {
		tab();
 		String xtn = printLinks() ? "#"+n.uniqueId : "";
 		ps.print("switch"+xtn+" (");
		indent();
		print(n.exp);
		ps.println(")");
		tab();
		ps.println("{");
		indent();
 		if (n.stmts == null) {
 			tab();
 			ps.println("??null??");
 		}
 		else {
 			print(n.stmts);
 		}
		unindent();
		tab();
		ps.println("}");
 		unindent();
		return null;
	}
	
	@Override
	public Object visitThis(This n) {
		ps.print("this"+typ(n.type));
		return null;
	}
	
	@Override
	public Object visitTimes(Times n) {
		binOp(n, "*");
		return null;
	}
	
	@Override
	public Object visitTrue(True n) {
		ps.print("true"+typ(n.type));
		return null;
	}
	
	@Override
	public Object visitVoidType(VoidType n) {
		ps.print("void");
		return null;
	}
	@Override
	public Object visitWhile(While n) {
		tab();
 		String xtn = printLinks() ? "#"+n.uniqueId : "";
 		ps.print("while"+xtn+" (");
		print(n.exp);
		ps.println(")");
		indent();
		print(n.body);
		unindent();
		return null;
	}
	
	public boolean printLinks() {
		return printLinks;
	}
	
	public void indent() {
		indent++;
	}
	public void unindent() {
		indent--;
	}
	public void tab() {
		for (int i = 0; i < indent; i++) {
			ps.print("  ");
		}
	}
	
	public void print(AstNode node) {
		if (node == null) {
			ps.print("??null??");
		}
		else {
			node.accept(this);
		}
	}

	public void print(AstList list) {
		if (list == null) {
			ps.print("??null??");
		}
		else {
			for (Object node : list) {
				if (node instanceof AstNode) {
					print((AstNode)node);
				}
				else {
					ps.println("***???***");
				}
			}
		}
	}
	
	public String typ(Type t) {
		if (!printTypes) {
			return "";
		}
		else {
			return "\\" + typString(t) + "\\";
		}
	}
	
	public String typString(Type t) {
		if (!printTypes) {
			return "";
		}
		else if (t == null) {
			return "??null??";
		}
		else if (t instanceof NullType) {
			return "n";
		}
		else if (t instanceof IntegerType) {
			return "i";
		}
		else if (t instanceof BooleanType) {
			return "b";
		}
		else if (t instanceof VoidType) {
			return "v";
		}
		else if (t instanceof ArrayType) {
			int count = 1;
			Type baseType = t;
			for (;;) {
				baseType = ((ArrayType)baseType).baseType;
				if (!(baseType instanceof ArrayType)) break;
				count++;
			}
			return count+typString(baseType);
		}
		else if (t instanceof IdentifierType) {
			return t.toString2();
		}
		else {
			return "??unknown??";
		}
	}

}
