import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class CompilationEngine {
	
	// fields for CompilationEngine
	// FileWriter fileWriter;
	JackTokenizer tokenizer;
	SymbolTable classTable, subroutineTable;
	VMWriter codeWriter;
	int tab, count, argCount, varCount, whileCount, ifCount;
	String token, filename, returnType;
	File[] fList;
	
	public CompilationEngine(String input) throws IOException {
		count = 0;
		whileCount = 0;
		ifCount = 0;
		File file = new File(input);
		
		if (file.isDirectory()) {
			fList = file.listFiles();
			for (File f : fList) {
				if (f.getName().contains(".jack")) {
					filename = f.getName().substring(0,f.getName().indexOf("."));
					System.out.println(filename);
					codeWriter = new VMWriter(filename + ".vm");
					tokenizer = new JackTokenizer(f);
					classTable = new SymbolTable();
					subroutineTable = new SymbolTable();
					tab = 0;
					compileClass();
					tokenizer.close();
					codeWriter.close();
				}
			}
		} else {
			filename = file.getName().substring(0,file.getName().indexOf("."));
			System.out.println(filename);
			codeWriter = new VMWriter(filename + ".vm");
			System.out.println(filename + ".vm");
			tokenizer = new JackTokenizer(file);
			tab = 0;
			compileClass();
			tokenizer.close();
			codeWriter.close();
		}				
	}
	
	public void write() throws IOException {
		// System.out.println(tab(tab) + tokenizer.writeXML());
		tokenizer.advance();
		token = tokenizer.curToken();
	}
	
	public void compileClass() throws IOException {
		tab++;
		tokenizer.advance();
		//class
		write();
		//className
		write();
		// {
		write();
		
		//classVarDec
		CompileClassVarDec();
		
		// subroutineDec;
		CompileSubroutine();
		
		// }
		write();
		
	}
	
	public String tab(int num) {
		String ret = "";
		for (int i = 0; i < num; i++) {
			ret = ret + "  ";
		}
		return ret;
	}
	
	public void CompileClassVarDec() throws IOException {
		while (token.equals("static") || token.equals("field")) {
			tab++;
			symbolTableInput("", ";");
			write();
			
			tab--;
		}
	}
	
	public void CompileSubroutine() throws IOException {
		while (token.equals("constructor") || token.equals("function") || token.equals("method")) {
			subroutineTable.startSubroutine();
			// fileWriter.write(tab(tab) + "<subroutineDec>" + "\n");
			tab++;
			if (token.equals("method")) {
				subroutineTable.define("argument", filename, "this");
				// System.out.println("argument" + "_" + filename + "_" + "this" + "_" + classTable.varCount("argument") + "_" + subroutineTable.varCount("argument"));
				// fileWriter.write("push argument 0" + "\n");
				// fileWriter.write("pop pointer 0" + "\n");
			}
			
			String subroutineType = token;
			// type
			write();
			returnType = token;

			// everything else up to (
			/*
			while(!token.equals("(")) {
				write();
			}
			*/
			
			// name
			write();
			String subroutineName = token;
			

			// (
			write();
			compileParameterList();
			/*
			if (subroutineType.equals("function")) {
				// declaration
				try {
					codeWriter.writeFunction(filename + "." + subroutineName, subroutineTable.IndexOf(filename));
				} catch (NullPointerException npe) {
					codeWriter.writeFunction(filename + "." + subroutineName, 0);
				}
				
			}
			*/
			
			// )
			write();
			
			// subroutineBody
			// fileWriter.write(tab(tab) + "<subroutineBody>" + "\n");
			tab++;
			// {
			write();
			// varDec
			compileVarDec();  // creates symbol table
			//if (subroutineType.equals("function")) {}
				// declaration
			
			if (subroutineType.equals("constructor")) {
				codeWriter.writeFunction(filename + "." + subroutineName, varCount);
				codeWriter.writePush("constant", varCount + 1);
				// create space in memory for obj + set base address segment to new obj address
				codeWriter.writeCall("Memomy.alloc ", 1);
				codeWriter.writePop("pointer", 0);
			} else if (subroutineType.equals("function")) {
				codeWriter.writeFunction(filename + "." + subroutineName, varCount);
			} else {
				subroutineTable.define("argument",  filename,  "this");
				codeWriter.writeFunction(filename + "." + "function", varCount + 1);
			}
			// statements
			compileStatement();
			
			// constructor: return objects base address
			codeWriter.writePush("pointer", 0);
			// }
			write();
			tab--;
			// fileWriter.write(tab(tab) + "</subroutineBody>" + "\n");
			
			tab--;
			// fileWriter.write(tab(tab) + "</subroutineDec>" + "\n");
		}
	}
	
	public void compileParameterList() throws IOException {
		// fileWriter.write(tab(tab) + "<parameterList>" + "\n");
		tab++;
		// (
		write();
//		while (!token.equals(")") ) {
//			write();
//		}
		symbolTableInput("argument", ")");
		tab--;
		// fileWriter.write(tab(tab) + "</parameterList>" + "\n");
	}
	
	public void compileVarDec() throws IOException {
		varCount = 0;
		while (token.equals("var")) {
			tab++;
			symbolTableInput("", ";");
			write();
			tab--;
		}
	}
	
	public void compileDo() throws IOException {
		// fileWriter.write(tab(tab) + "<doStatement>" + "\n");
		tab++;
		// write until expressionlist
		String routineName = "";
		write();
		
		if (subroutineTable.table.containsKey(token)) {
			routineName = subroutineTable.TypeOf(token);
			codeWriter.writePush(subroutineTable.KindOf(token), subroutineTable.IndexOf(token));
			write();
		}
		/*
		while (!token.equals("(")) {
			routineName = routineName.concat(token);
			write();
		}
		*/
		if (token.equals(".")) {
			write();
			routineName = routineName.concat("." + token);
		} else { routineName = filename.concat("." + token); }
		// (
		write();
		write();
		if (!token.equals(")")) {
			compileExpressionList();
			codeWriter.writeCall(routineName, argCount);
		} else {
			codeWriter.writeCall(routineName, argCount);
		}
		// )
		write();
		// ;
		write();
		
		codeWriter.writePop("temp", 0);
		tab--;
		// fileWriter.write(tab(tab) + "</doStatement>" + "\n");
	}
	
	public void compileLet() throws IOException {
		// fileWriter.write(tab(tab) + "<letStatement>" + "\n");
		tab++;
		// let
		write();
		//varName
		String varName = token;
		write();
		if (token.equals("[")) {
			write();
			compileExpression();
			write();
		}
		// =
		write();
		compileExpression();
		// ;
		write();
		try {
			codeWriter.writePop(subroutineTable.KindOf(varName), subroutineTable.IndexOf(varName));
		} catch (NullPointerException npe) {
			codeWriter.writePop(classTable.KindOf(varName), classTable.IndexOf(varName));
		}
		tab--;
		// fileWriter.write(tab(tab) + "</letStatement>" + "\n");
	}
	
	public void compileWhile() throws IOException {
		// fileWriter.write(tab(tab) + "<whileStatement>" + "\n");
		tab++;
		// while
		write();
		codeWriter.writeLabel("WHILE_EXP" + whileCount);
		// (
		// write();
		compileExpression();
		// )
		// write();
		codeWriter.writeArithmetic("~");
		codeWriter.writeIf("WHILE_END" + whileCount);
		// {
		write();
		// statement
		compileStatement();
		// }
		write();
		codeWriter.writeGoto("WHILE_EXP" + whileCount);
		codeWriter.writeLabel("WHILE_END" + whileCount);
		whileCount++;
		tab--;
		// fileWriter.write(tab(tab) + "</whileStatement>" + "\n");
	}
	
	public void compileReturn() throws IOException {
		// fileWriter.write(tab(tab) + "<returnStatement>" + "\n");
		tab++;
		if (returnType.equals("void")) {
			codeWriter.writePush("constant", 0);
		}
		// return
		write();
		if (!token.equals(";")) {
			compileExpression();
		}
		codeWriter.writeReturn();
		// ;
		write();
		tab--;
		// fileWriter.write(tab(tab) + "</returnStatement>" + "\n");
	}
	
	public void compileIf() throws IOException {
		// fileWriter.write(tab(tab) + "<ifStatement>" + "\n");
		ifCount = 0;
		tab++;
		// if
		write();
		int currentIfCount = 0;
		// (
		write();
		compileExpression();
		// )
		write();
		codeWriter.writeIf("IF_TRUE" + currentIfCount);
		codeWriter.writeGoto("IF_FALSE" + currentIfCount);
		codeWriter.writeLabel("IF_TRUE" + currentIfCount);
		// {
		write();
		// Statement
		compileStatement();
		// }
		codeWriter.writeGoto("IF_END" + currentIfCount);
		codeWriter.writeLabel("IF_FALSE" + currentIfCount);
		write();
		if (token.equals("else")) {
			// else
			write();
			// {
			write();
			compileStatement();
			// }
			write();
		}
		
		codeWriter.writeLabel("IF_END" + currentIfCount);
		
		tab--;
		// fileWriter.write(tab(tab) + "</ifStatement>" + "\n");
	}
	
	public void compileExpression() throws IOException {
		// fileWriter.write(tab(tab) + "<expression>" + "\n");
		tab++;
		compileTerm();
		// 3) if exp is exp1 op exp2
		while (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") || token.equals("&amp;") || token.equals("|") ||
			token.equals("&lt;") || token.equals("&gt;") || token.equals("=")) {
			// op
			String op = token;
			write();
			compileTerm();
			codeWriter.writeArithmetic(op);
					
		}
		tab--;
		// fileWriter.write(tab(tab) + "</expression>" + "\n");
	}
	
	public void compileTerm() throws IOException {
		// fileWriter.write(tab(tab) + "<term>" + "\n");
		tab++;
		// (expression)
		if (token.equals("(")) {
			// (
			write();
			compileExpression();
			// )
			write();
			
			tab--;
			// fileWriter.write(tab(tab) + "</term>" + "\n");
			return;
		}
		
		// 4) unaryOp if exp is op exp
		if (token.equals("-") || token.equals("~")) {
			// -
			String op = token;
			write();
			compileTerm();
			if (op.equals("-")) {
				codeWriter.writeArithmetic("neg");
			} else { codeWriter.writeArithmetic("~"); }
			tab--;
			return;
		} 
		
		// intergerConstant | stringConstant | keywordConstant | subroutinName | varName
		String className = token;
		try {
			// 1) if exp is number push
			int e1 = Integer.parseInt(token);
			codeWriter.writePush("constant", e1);
		} catch (NumberFormatException nfe) {
			// 2) if exp is var push
			if (subroutineTable.table.containsKey(token)) {
				codeWriter.writePush(subroutineTable.KindOf(token), subroutineTable.IndexOf(token));
			} else if (classTable.table.containsKey(token)) {
				codeWriter.writePush(classTable.KindOf(token), classTable.IndexOf(token));
			} else if (token.equals("true")) {
				codeWriter.writePush("constant", 0);
				codeWriter.writeArithmetic("~");
			} else if (token.equals("false")) {
				codeWriter.writePush("constant", 0);
			}
			
		}
		write(); 
	
		// subroutineCall
		if (token.equals("(")) {
			// (
			write();
			compileExpressionList();
			// )
			write();
			// className|varName
			write();
			// .
			write();
			// subroutineName
			write();
			// (
			write();
			compileExpressionList();
			// )
			write();
			
			tab--;
			// fileWriter.write(tab(tab) + "</term>" + "\n");
			return;
		}
		//varName []
		if (token.equals("[")) {
			// [
			write();
			compileExpression();
			// ]
			write();
			
			tab--;
			// fileWriter.write(tab(tab) + "</term>" + "\n");
			return;
		}
		
		// x.x
		if (token.equals(".")) {
			// .
			write();
			// name
			String subroutineName = token;
			write();
			// (
			write();
			compileExpressionList();
			// )
			write();
			codeWriter.writeCall(className + "." + subroutineName, argCount);
		}

		tab--;
		// fileWriter.write(tab(tab) + "</term>" + "\n");
	}
	
	public void compileExpressionList() throws IOException {
		// fileWriter.write(tab(tab) + "<expressionList>" + "\n");
		tab++;
		argCount = 0;
		while (!token.equals(")") && !token.isEmpty()) {
			compileExpression();
			argCount++;
			if (token.equals(",")) {
				// write commas
				write();
			}
		}
		tab--;
		// fileWriter.write(tab(tab) + "</expressionList>" + "\n");
	}
	
	public void compileStatement() throws IOException {
		// fileWriter.write(tab(tab) + "<statements>" + "\n");
		tab++;
		boolean flag2 = true;
		while(flag2) {
			switch (token) {
			case "let":
				compileLet();
				break;
			case "if":
				compileIf();
				break;
			case "while":
				compileWhile();
				break;
			case "do":
				compileDo();
				break;
			case "return":
				compileReturn();
				break;
			default:
				flag2 = false;
			}
		}
		tab--;
		// fileWriter.write(tab(tab) + "</statements>" + "\n");
	}
	
	private void symbolTableInput(String kind, String delim) throws IOException {
		String type = "";
		String name = "";
		while(!token.equals(delim)) {
			if (tokenizer.tokenType().equals("symbol")) {
				
			} else if (kind.isEmpty()) {
				kind = token;
				if (kind.equals("var")) {
					kind = "local";
				}
			} else if (type.isEmpty()) {
				//if (tokenizer.tokenType().equals("keyword")) {
					type = token;
				//} else if (!tokenizer.tokenType().equals("symbol")) { type = filename; }
			} else if (name.isEmpty()) {
				if (!tokenizer.tokenType().equals("symbol")) {
					name = token;
				}
			}
			if (!name.isEmpty()) {
				varCount++;
				if (kind.equals("static") || kind.equals("field")) {
					classTable.define(kind, type, name);
				} else { subroutineTable.define(kind,  type,  name); }
				System.out.println(kind + "_" + type + "_" + name + "_" + classTable.varCount(kind) + "_" + subroutineTable.varCount(kind));
				name = "";
				if (kind.equals("argument")) { type = ""; }
			}
			
			write();
		}
	}
}
