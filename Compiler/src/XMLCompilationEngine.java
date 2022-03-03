import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Scanner;

public class XMLCompilationEngine {


	// fields for CompilationEngine
	FileWriter fileWriter;
	JackTokenizer tokenizer;
	SymbolTable classTable, subroutineTable;
	int tab, count;
	String token, filename, kind, type, name;
	File[] fList;

	public XMLCompilationEngine(String input) throws IOException {
		count = 0;
		File file = new File(input);

		if (file.isDirectory()) {
			fList = file.listFiles();
			for (File f : fList) {
				if (f.getName().contains(".jack")) {
					filename = f.getName().substring(0,f.getName().indexOf("."));
					System.out.println(filename);
					fileWriter = new FileWriter(filename + ".xml");
					tokenizer = new JackTokenizer(f);
					classTable = new SymbolTable();
					subroutineTable = new SymbolTable();
					tab = 0;
					compileClass();
					tokenizer.close();
					fileWriter.close();
				}
			}
		} else {
			filename = file.getName().substring(0,file.getName().indexOf("."));
			System.out.println(filename);
			fileWriter = new FileWriter(filename + ".xml");
			System.out.println(filename + ".xml");
			tokenizer = new JackTokenizer(file);
			tab = 0;
			compileClass();
			fileWriter.close();
		}				
	}

	public void write() throws IOException {
		fileWriter.write(tab(tab) + tokenizer.writeXML() + "\n");
		tokenizer.advance();
		token = tokenizer.curToken();
	}

	public void compileClass() throws IOException {
		fileWriter.write("<class>" + "\n");
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


		fileWriter.write("</class>" + "\n");
		fileWriter.close();
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
			fileWriter.write(tab(tab) + "<classVarDec>" + "\n");
			tab++;
			symbolTableInput("", ";");
			write();

			tab--;
			fileWriter.write(tab(tab) + "</classVarDec>" + "\n");
		}
	}

	public void CompileSubroutine() throws IOException {
		while (token.equals("constructor") || token.equals("function") || token.equals("method")) {
			subroutineTable.startSubroutine();
			fileWriter.write(tab(tab) + "<subroutineDec>" + "\n");
			tab++;
			if (token.equals("method")) {
				subroutineTable.define("arg", filename, "this");
				System.out.println("arg" + "_" + filename + "_" + "this" + "_" + classTable.varCount("arg") + "_" + subroutineTable.varCount("arg"));
			}
			// declaration
			write();
			// everything else up to {
			while(!token.equals("(")) {
				write();
			}
			// (
			write();
			compileParameterList();
			// )
			write();

			// subroutineBody
			fileWriter.write(tab(tab) + "<subroutineBody>" + "\n");
			tab++;
			// {
			write();
			// varDec
			compileVarDec();
			// statements
			compileStatement();
			// }
			write();
			tab--;
			fileWriter.write(tab(tab) + "</subroutineBody>" + "\n");

			tab--;
			fileWriter.write(tab(tab) + "</subroutineDec>" + "\n");
		}
	}

	public void compileParameterList() throws IOException {
		fileWriter.write(tab(tab) + "<parameterList>" + "\n");
		tab++;
		//			while (!token.equals(")") ) {
		//				write();
		//			}

		symbolTableInput("arg", ")");
		tab--;
		fileWriter.write(tab(tab) + "</parameterList>" + "\n");
	}

	public void compileVarDec() throws IOException {		
		while (token.equals("var")) {
			fileWriter.write(tab(tab) + "<varDec>" + "\n");
			tab++;
			symbolTableInput("", ";");
			write();
			tab--;
			fileWriter.write(tab(tab) + "</varDec>" + "\n");
		}
	}

	public void compileDo() throws IOException {
		fileWriter.write(tab(tab) + "<doStatement>" + "\n");
		tab++;
		// write until expressionlist
		while (!token.equals("(")) {
			write();
		}
		// (
		write();
		if (!token.equals(")")) {
			compileExpressionList();
		} else {
			fileWriter.write(tab(tab) + "<expressionList>" + "\n");
			fileWriter.write(tab(tab) + "</expressionList>" + "\n");
		}
		// )
		write();
		// ;
		write();
		tab--;
		fileWriter.write(tab(tab) + "</doStatement>" + "\n");
	}

	public void compileLet() throws IOException {
		fileWriter.write(tab(tab) + "<letStatement>" + "\n");
		tab++;
		// let
		write();
		//varName
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
		tab--;
		fileWriter.write(tab(tab) + "</letStatement>" + "\n");
	}

	public void compileWhile() throws IOException {
		fileWriter.write(tab(tab) + "<whileStatement>" + "\n");
		tab++;
		// while
		write();
		// (
		write();
		compileExpression();
		// )
		write();
		// {
		write();
		// statement
		compileStatement();
		// }
		write();
		tab--;
		fileWriter.write(tab(tab) + "</whileStatement>" + "\n");
	}

	public void compileReturn() throws IOException {
		fileWriter.write(tab(tab) + "<returnStatement>" + "\n");
		tab++;
		// return
		write();
		if (!token.equals(";")) {
			compileExpression();
		}
		// ;
		write();
		tab--;
		fileWriter.write(tab(tab) + "</returnStatement>" + "\n");
	}

	public void compileIf() throws IOException {
		fileWriter.write(tab(tab) + "<ifStatement>" + "\n");
		tab++;
		// if
		write();
		// (
		write();
		compileExpression();
		// )
		write();
		// {
		write();
		// Statement
		compileStatement();
		// }
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
		tab--;
		fileWriter.write(tab(tab) + "</ifStatement>" + "\n");
	}

	public void compileExpression() throws IOException {
		fileWriter.write(tab(tab) + "<expression>" + "\n");
		tab++;
		compileTerm();
		while (token.equals("+") || token.equals("-") || token.equals("*") || token.equals("/") || token.equals("&amp;") || token.equals("|") ||
				token.equals("&lt;") || token.equals("&gt;") || token.equals("=")) {
			// op
			write();
			compileTerm();
		}
		tab--;
		fileWriter.write(tab(tab) + "</expression>" + "\n");
	}

	public void compileTerm() throws IOException {
		fileWriter.write(tab(tab) + "<term>" + "\n");
		tab++;
		// (expression)
		if (token.equals("(")) {
			// (
			write();
			compileExpression();
			// )
			write();

			tab--;
			fileWriter.write(tab(tab) + "</term>" + "\n");
			return;
		}

		// unaryOp 
		if (token.equals("-") || token.equals("~")) {
			// -
			write();
			compileTerm();
			tab--;
			fileWriter.write(tab(tab) + "</term>" + "\n");
			return;
		} 

		// intergerConstant | stringConstant | keywordConstant | subroutinName | varName
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
			fileWriter.write(tab(tab) + "</term>" + "\n");
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
			fileWriter.write(tab(tab) + "</term>" + "\n");
			return;
		}

		// x.x
		if (token.equals(".")) {
			// .
			write();
			// name
			write();
			// (
			write();
			compileExpressionList();
			// )
			write();
		}

		tab--;
		fileWriter.write(tab(tab) + "</term>" + "\n");
	}

	public void compileExpressionList() throws IOException {
		fileWriter.write(tab(tab) + "<expressionList>" + "\n");
		tab++;
		while (!token.equals(")") && !token.isEmpty()) {
			compileExpression();
			if (token.equals(",")) {
				// write commas
				write();
			}
		}
		tab--;
		fileWriter.write(tab(tab) + "</expressionList>" + "\n");
	}

	public void compileStatement() throws IOException {
		fileWriter.write(tab(tab) + "<statements>" + "\n");
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
		fileWriter.write(tab(tab) + "</statements>" + "\n");
	}

	private void symbolTableInput(String kind, String delim) throws IOException {
		type = "";
		name = "";
		while(!token.equals(delim)) {
			if (kind.isEmpty()) {
				kind = token;
			} else if (type.isEmpty()) {
				if (tokenizer.tokenType().equals("keyword")) {
					type = token;
				} else if (!tokenizer.tokenType().equals("symbol")) { type = filename; }
			} else if (name.isEmpty()) {
				if (!tokenizer.tokenType().equals("symbol")) {
					name = token;
				}
			}

			if (!name.isEmpty()) {
				if (kind.equals("static") || kind.equals("field")) {
					classTable.define(kind, type, name);
				} else { subroutineTable.define(kind,  type,  name); }
				System.out.println(kind + "_" + type + "_" + name + "_" + classTable.varCount(kind) + "_" + subroutineTable.varCount(kind));
				name = "";
				if (kind.equals("arg")) { type = ""; }
			}

			write();
		}
	}
}
