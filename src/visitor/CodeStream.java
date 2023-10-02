package visitor;

import java.io.PrintStream;
import syntaxtree.AstNode;
import errorMsg.ErrorMsg;

public class CodeStream {
	private PrintStream out;
	private ErrorMsg err;
	String indentStr;
	CG3Visitor visitor3;
		
	public CodeStream(PrintStream ps, ErrorMsg e) {
		out = ps;
		err = e;
		indentStr = "";
	}
	public void flush() {
		out.flush();
	}
	
	public void setVisitor3(CG3Visitor vis) {
		visitor3 = vis;
	}
	
	public void emit(AstNode node, String str) {
		int pos = -1;
		String className = "";
		if (node != null) {
			className = ""+node.getClass();
			int lastDotSpot = className.lastIndexOf(".");
			className = className.substring(lastDotSpot+1);
			pos = node.pos;
		}
		out.print(indentStr+str+" # "+className+" at "+err.lineAndChar(pos));
		if (visitor3 != null) {
			out.print("; stackHeight = "+visitor3.stackHeight);
		}
		out.println();
	}
	
	public void indent(AstNode n) {
		indentStr += "  ";
		emit(n, "# ENTER NODE");
	}
	public void unindent(AstNode n) {
		emit(n, "# EXIT NODE");
		indentStr = indentStr.substring(Math.min(2,indentStr.length()));
	}
	
	public ErrorMsg getErrorMsg() {
		return err;
	}
	
}