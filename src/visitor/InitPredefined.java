package visitor;

import syntaxtree.BooleanType;
import syntaxtree.ClassDecl;
import syntaxtree.ClassDeclList;
import syntaxtree.DeclList;
import syntaxtree.Exp;
import syntaxtree.False;
import syntaxtree.FormalDecl;
import syntaxtree.IdentifierType;
import syntaxtree.IntegerLiteral;
import syntaxtree.IntegerType;
import syntaxtree.MethodDecl;
import syntaxtree.MethodDeclNonVoid;
import syntaxtree.MethodDeclVoid;
import syntaxtree.Null;
import syntaxtree.StatementList;
import syntaxtree.Type;
import syntaxtree.VarDecl;
import syntaxtree.VarDeclList;
import syntaxtree.VoidType;
import java.util.Hashtable;
import errorMsg.ErrorMsg;

public class InitPredefined {
	
	private static String[] predefinedClassNames = {
			"Object", "String", "Lib", "RunMain",
	};
	
	// returns a dummy symbol table
	static Hashtable<String,ClassDecl> initGlobalSymTab(Hashtable<String,ClassDecl> globalSymTab,
						ErrorMsg errorMsg, ClassDeclList dummyDecls) {

		ClassDecl classObjectDecl = null;
		ClassDecl classStringDecl = null;
		ClassDecl classLibDecl = null;
		
		Hashtable<String,ClassDecl> rtnVal = new Hashtable<String,ClassDecl>();	
		for (String name : predefinedClassNames) {
			ClassDecl cd = createClass(name, name.equals("Object") ? "" : "Object");
			rtnVal.put(name,  cd);
			switch (name) {
			case "Object": classObjectDecl = cd; break;
			case "String": classStringDecl = cd; break;
			case "Lib": classLibDecl = cd; break;
			}
			dummyDecls.add(cd);
		}
		
		addDummyMethod(classObjectDecl, "hashCode", "int", new String[]{});
		addDummyMethod(classObjectDecl, "equals", "boolean", new String[]{"Object"});
		addDummyMethod(classObjectDecl, "toString", "String", new String[]{});
	    addDummyMethod(classLibDecl, "readLine", "String", new String[]{});
	    addDummyMethod(classLibDecl, "readInt", "int", new String[]{});
	    addDummyMethod(classLibDecl, "readChar", "int", new String[]{});
		addDummyMethod(classLibDecl, "printStr", "void", new String[]{"String"});
		addDummyMethod(classLibDecl, "printBool", "void", new String[]{"boolean"});
		addDummyMethod(classLibDecl, "printInt", "void", new String[]{"int"});
		addDummyMethod(classLibDecl, "intToString", "String",
				new String[]{"int"});
		addDummyMethod(classLibDecl, "intToChar", "String",
				new String[]{"int"});
		addDummyMethod(classStringDecl, "hashCode", "int", new String[]{});
		addDummyMethod(classStringDecl, "equals", "boolean", new String[]{"Object"});
		addDummyMethod(classStringDecl, "toString", "String", new String[]{});
		addDummyMethod(classStringDecl, "concat", "String",
				new String[]{"String"});
		addDummyMethod(classStringDecl, "substring", "String",
				new String[]{"int","int"});
		addDummyMethod(classStringDecl, "length", "int", new String[]{});
		addDummyMethod(classStringDecl, "charAt", "int",
				new String[]{"int"});
		addDummyMethod(classStringDecl, "compareTo", "int",
				new String[]{"String"});
		
		return rtnVal;

//		vis.visitClassDecl(classObjectDecl);
//		vis.visitClassDecl(classLibDecl);
//		vis.visitClassDecl(classStringDecl);
//		vis.visitClassDecl(classRunMainDecl);
//
//		Sem2Visitor s2 = new Sem2Visitor(globalSymTab, errorMsg);
//		s2.visit(classObjectDecl);
//		s2.visit(classLibDecl);
//		s2.visit(classStringDecl);
//		s2.visit(classRunMainDecl);
//		
//		Sem3Visitor s3 = new Sem3Visitor(globalSymTab, errorMsg);
//		s3.visit(classObjectDecl);
//		s3.visit(classLibDecl);
//		s3.visit(classStringDecl);
//		s3.visit(classRunMainDecl);
	}
	
	private static ClassDecl createClass(String name, String superName) {
		return new ClassDecl(-1, name, superName, new DeclList());
	}
 	
	private static void addDummyMethod(ClassDecl dec, String methName,
				String rtnTypeName, String[] parmTypeNames) {
		VarDeclList parmDecls = new VarDeclList();
		for (int i = 0; i < parmTypeNames.length; i++) {
			Type t = convertToType(parmTypeNames[i]);
			String parmName = "parm"+i;
			VarDecl vd = new FormalDecl(-1, t, parmName);
			parmDecls.addElement(vd);
		}
		Type t = convertToType(rtnTypeName);
		StatementList sl = new StatementList(); // dummied up
		MethodDecl md;
		if (t == null || t instanceof VoidType) { // void return-type
			md = new MethodDeclVoid(-1, methName, parmDecls, sl);
		}
		else { // non-void return type
			Exp rtnExpr;
			if (t instanceof IntegerType) {
				rtnExpr = new IntegerLiteral(-1,0);
			}
			else if (t instanceof BooleanType) {
				rtnExpr = new False(-1);
			}
			else {
				rtnExpr = new Null(-1);
			}
			md = new MethodDeclNonVoid(-1, t, methName, parmDecls, sl, rtnExpr);
		}
		dec.decls.addElement(md);
	}
	
	private static Type convertToType(String s) {
		if (s.equals("void")) {
			return new VoidType(-1);
		}
		else if (s.equals("boolean")) {
			return new BooleanType(-1);
		}
		else if (s.equals("int")) {
			return new IntegerType(-1);
		}
		else {
			return new IdentifierType(-1, s);
		}
	}
	
//	static void traversePredefinedClasses(ASTvisitor vis,
//			Hashtable<String,ClassDecl> globalSymTab) {
//		for (String name : predefinedClassNames) {
//			ClassDecl cd = globalSymTab.get(name);
//			cd.accept(vis);
//		}
//	}

}
