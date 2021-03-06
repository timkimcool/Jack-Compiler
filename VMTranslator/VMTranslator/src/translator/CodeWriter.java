package translator;

import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CodeWriter {
	private static final String FORM1 =
			"@SP\n" +
			"AM=M-1\n" +
			"D=M\n" +
			"A=A-1\n";
	private static final String ADD =
			FORM1 +
			"M=D+M\n";

	private static final String SUB =
			FORM1 +
			"M=M-D\n";

	private static final String NEG =
			"@SP\n" +
			"A=M-1\n" +
			"M=-M\n";

	private static final String AND =
			"@SP\n" +
			"AM=M-1\n" +
			"D=M\n" +
			"A=A-1\n" +
			"M=D&M\n";

	private static final String OR =
			"@SP\n" +
			"AM=M-1\n" +
			"D=M\n" +
			"A=A-1\n" +
			"M=D|M\n";

	private static final String NOT =
			"@SP\n" +
			"A=M-1\n" +
			"M=!M\n";

	// push D -> *SP++
	private static final String PUSH =
			"D=M\n" +
			"@SP\n" +
			"AM=M+1\n" +
			"A=A-1\n" +
			"M=D\n";

	// pop *SP-- -> *D
	private static final String POP =
			"D=A\n" +
			"@R13\n" +
			"M=D\n" +
			"@SP\n" +
			"AM=M-1\n" +
			"D=M\n" +
			"@R13\n" +
			"A=M\n" +
			"M=D\n";
	  
	private FileWriter print;
	private Map<String, String> map;
	private String funcName;
	private int fcounter;
	private int rCounter;
	private String fileName;
	
	public CodeWriter(String s) throws IOException {
		this.print = new FileWriter(s);
		map = new HashMap<String, String>();
		fcounter = 0;
		rCounter = 0;
		map.put("local", "LCL");
		map.put("argument", "ARG");
		map.put("this", "THIS");
		map.put("that", "THAT");
	}
	
	public void setFileName(String s) {
		fileName=s.substring(s.lastIndexOf("\\") + 1,s.indexOf(".vm"));
	}
	
	public void process (String s) throws IOException {
		String ret = "//" + s + "\n";

		// argument handling
		String[] arr = s.split(" ");
		String arg1 = null;
		int arg2 = 0;
		
		if (arr.length >= 2) {
			arg1 = arr[1];
		}
		if (arr.length == 3) {
			arg2 = Integer.parseInt(arr[2]);
		}
		if(map.containsKey(arg1)) {
			arg1 = map.get(arg1);
		}
		String f = arr[0];
		
		switch (f) {
			case "push":
				// special handling for constant push
				if (arg1.equals("constant")) {
					ret +=
						"@" + arg2 + "\n" +
						"D=A\n" +
						"@SP\n" +
						"AM=M+1\n" +
						"A=A-1\n" +
						"M=D\n";
				} else {
					ret += writePP(arg1, arg2) + PUSH;
				}
				break;
			case "pop":
				ret += writePP(arg1, arg2) + POP;
				break;
			case "label":
				ret += writeLabel(arg1);
				break;
			case "goto":
				ret += writeGoto(arg1);
				break;
			case "if-goto":
				ret += writeIf(arg1);
				break;
			case "function":
				funcName= arg1;
				ret += writeFunction(arg1, arg2);
				break;
			case "return":
				ret += writeReturn();
				break;
			case "call":
				ret += writeCall(arg1, arg2);
				break;
			default:
				ret += writeArithmetic(s);
		}
		print.write(ret);
	}
	
	public String writeArithmetic(String s) {
		switch (s) {
		case "add":
			return ADD;
		case "sub":
			return SUB;
		case "neg":
			return NEG;
		case "eq":
			return writeCondition("JEQ");
		case "gt":
			return writeCondition("JGT");
		case "lt":
			return writeCondition("JLT");
		case "and":
			return AND;
		case "or":
			return OR;
		case "not":
			return NOT;
		default: return "";
		}
		
	}
	
	public String writeCondition(String j) {
		fcounter++;
		return
			FORM1 +
			"D=M-D\n" +
			"@TRUE_" + fcounter + "\n" +
			"D;" + j + "\n" +
			"@SP\n" +
			"A=M-1\n" +
			"M=0\n" +
			"@CONTINUE_" + fcounter + "\n" +
			"0;JMP\n" +
			"(TRUE_" + fcounter +")\n" +
			"@SP\n" +
			"A=M-1\n" +
			"M=-1\n" +
			"(CONTINUE_" + fcounter +")\n";
	}
	
	public String writePP(String arg1, int arg2) {
		//LCL, ARG, THIS, THAT
		if (arg1.equals("LCL") || arg1.equals("ARG")  || arg1.equals("THIS")  || arg1.equals("THAT")) {
			return
				"@" + arg1 + "\n" +
				"D=M\n" +
				"@" + Integer.toString(arg2) + "\n" +
				"A=D+A\n";
		}
		//pointer
		if (arg1.equals("pointer")) {
			if (arg2 == 0) {
				return
					"@THIS\n";
			} else {
				return
					"@THAT\n";
			}
		}
		// temp
		if (arg1.equals("temp")) {
			arg2 += 5;
			return
				"@" + arg2 + "\n";
		}
		// static
		if (arg1.equals("static")) {
			return
				"@" + fileName + "." + arg2 + "\n";
		}
		
		return "bad command";
	}
	
	public String writeFunction(String fName, int numVars) {
		String ret = 
				"(" + fName + ")\n" +
				"@SP\n" +
				"A=M\n";
		for (int i = 0; i < numVars; i++) {
			ret +=
				"M=0\n" +
				"A=A+1\n";
		}
		
		return ret +
			"D=A\n" +
			"@SP\n" +
			"M=D\n";
	}
	
	public String writeCall(String fName, int numArgs) {
		rCounter++;
		numArgs = numArgs + 5;  // for repositioning ARG
		return
			// push returnAddress
			"@RETURN_ADDRESS_" + rCounter + "\n" +
			"D=A\n" +
			"@SP\n" +
			"A=M\n" +
			"M=D\n" +
			"@SP\n" +
			"M=M+1\n" +			
			// push LCL, ARG, THIS, THAT
			"// push LCL\n" +
			"@LCL\n" + PUSH +
			"// push ARG\n" +
			"@ARG\n" + PUSH +
			//writePP("ARG", 0) + PUSH +
			"// push THIS\n" +
			"@THIS\n" + PUSH +
			//writePP("THIS", 0) + PUSH +
			"// push THAT\n" +
			"@THAT\n" + PUSH +
			//writePP("THAT", 0) + PUSH +
			// reposition ARG
			"// reposition ARG\n" +
			"@SP\n" +
			"D=M\n" +
			"@" + numArgs + "\n" +
			"D=D-A\n" +
			"@ARG\n" +
			"M=D\n" +
			// LCL = SP
			"@SP\n" +
			"D=M\n" +
			"@LCL\n" +
			"M=D\n" +
			// goto function
			"@" + fName + "\n" +
			"0;JMP\n" +
			//label for return-address
			"(RETURN_ADDRESS_" + rCounter + ")\n";
	}
	
	public String writeReturn() {
		return
			// FRAME = LCL (temp var)
			"@LCL\n" +
			"D=M\n" +
			"@FRAME\n" +
			"M=D\n" +
			// RET = *(FRAME-5); ret-addr in temp var
			"@5\n" +
			"A=D-A\n" +
			"D=M\n" +
			"@RET\n" +
			"M=D\n" +
			// *ARG = pop(); reposition return value of caller
			"// ARG pop\n" +
			writePP("ARG",0) + POP +			
			// SP = ARG + 1; Restore SP
			"@ARG\n" +
			"D=M+1\n" +
			"@SP\n" +
			"M=D\n" +
			// Restore THAT, THIS, ARG, LCL of caller
			"@FRAME\n" +
			"D=M-1\n" +
			"AM=D\n" +
			"D=M\n" +
			"@THAT\n" +
			"M=D\n" +
			"@FRAME\n" +
			"D=M-1\n" +
			"AM=D\n" +
			"D=M\n" +
			"@THIS\n" +
			"M=D\n" +
			"@FRAME\n" +
			"D=M-1\n" +
			"AM=D\n" +
			"D=M\n" +
			"@ARG\n" +
			"M=D\n" +
			"@FRAME\n" +
			"D=M-1\n" +
			"AM=D\n" +
			"D=M\n" +
			"@LCL\n" +
			"M=D\n" +
			//goto RET; goto return-adddress in caller
			"@RET\n" +
			"A=M\n" +
			"0;JMP\n";
	}
	
	public String writeGoto(String label) {
		return 
			"@" + funcName + "$" + label + "\n" +
			"0;JMP\n";
	}
	
	public String writeIf(String label) {
		return
			"@SP\n" +
			"AM=M-1\n" +
			"D=M\n" +
			"@" + funcName + "$" + label + "\n" +
			"D;JNE\n";
	}
	
	public String writeLabel(String label) {
		return
			"(" + funcName + "$" + label + ")\n";
	}
	
	public void writeInit() throws IOException {
		String ret =
			"// bootstrap\n" +
			"@256\n" +
			"D=A\n" +
			"@SP\n" +
			"M=D\n" +
			"// call Sys.init 0\n";
		ret += writeCall("Sys.init",0);
		print.write(ret);
		//"0;JMP\n"
	}
	
	public void close() throws IOException {
		print.close();
	}
	
}
