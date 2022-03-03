import java.io.FileWriter;
import java.io.IOException;

public class VMWriter {
	FileWriter fileWriter;
	final String lb = "\n";
	
	public VMWriter(String filename) throws IOException {
		fileWriter = new FileWriter(filename);
	}
	

	
	public void writePush(String segment, int index) throws IOException {
		fileWriter.write("push " + segment + " " + index + lb);
	}
	
	public void writePop(String segment, int index) throws IOException {
		fileWriter.write("pop " + segment + " " + index + lb);
	}
	
	public void writeArithmetic(String command) throws IOException {
		switch (command) {
		case "+":
			fileWriter.write("add" + lb);
			break;
		case "-":
			fileWriter.write("sub" + lb);
			break;
		case "*":
			fileWriter.write("call Math.multiply 2" + lb);
			break;
		case "/":
			fileWriter.write("call Math.divide 2" + lb);
			break;
		case "&amp;":
			fileWriter.write("and" + lb);
			break;
		case "&lt;":
			fileWriter.write("lt" + lb);
			break;
		case "&gt;":
			fileWriter.write("gt" + lb);
			break;
		case "=":
			fileWriter.write("eq" + lb);
			break;
		case "~":
			fileWriter.write("not" + lb);
			break;
		case "neg":
			fileWriter.write("neg" + lb);
			break;
		}
	}
	
	public void writeLabel(String label) throws IOException {
		fileWriter.write("label " + label + lb);
	}
	
	public void writeGoto(String label) throws IOException {
		fileWriter.write("goto " + label + lb);
	}
	
	public void writeIf(String label) throws IOException {
		fileWriter.write("if-goto " + label + lb);
	}
	
	public void writeCall(String label, int nArgs) throws IOException {
		fileWriter.write("call " + label + " " + nArgs + lb);
	}
	
	public void writeFunction(String name, int nLocals) throws IOException {
		fileWriter.write("function " + name + " " + nLocals + lb);
	}
	
	public void writeReturn() throws IOException {
		fileWriter.write("return" + lb);
	}
	
	public void close() throws IOException {
		fileWriter.close();
	}
}
