package assembler;

import java.util.HashMap;
import java.util.Map;

public class Translator {
	
	private SymbolTable sT;
	private Map<String, String> cT;
	private Integer n;
	
	public Translator() {
		sT = new SymbolTable();
		cT = new HashMap<String, String>();
		n = 16;
		
		cT.put("0", "0101010");
		cT.put("1", "0111111");
		cT.put("-1","0111010");
		cT.put("D", "0001100");
		cT.put("A", "0110000");
		cT.put("!D", "0001101");
		cT.put("!A", "0110001");
		cT.put("-D", "0001111");
		cT.put("-A", "0110011");
		cT.put("D+1","0011111");
		cT.put("A+1","0110111");
		cT.put("D+A","0000010");
		cT.put("D-A","0010011");
		cT.put("A-D","0000111");
		cT.put("D&A","0000000");
		cT.put("D|A","0010101");	
		cT.put("M","1110000");
		cT.put("!M","1110001");
		cT.put("-M","1110011");
		cT.put("M+1","1110111");
		cT.put("M-1","1110010");
		cT.put("D+M","1000010");
		cT.put("D-M","1010011");
		cT.put("M-D","1000111");
		cT.put("D&M","1000000");
		cT.put("D|M","1010101");
		cT.put("D-1","0001110");
		cT.put("A-1","0110010");	
	}
	
	public void firstPass(Parser P) {
		int counter = 0;
		while (P.hasNextLine()) {
			String line = process(P.nextLine());
			if (line.isEmpty()) continue;
			if (line.charAt(0) == '(') {
				line = line.substring(1, line.indexOf(')'));
				if (!sT.exist(line)) {
					sT.put(line, counter);
				}
			} else counter++;
		}
	}
	
	public String Translate(String s) {
		if (s.charAt(0) == '@') {
			return aInstruction(s);
		}
		if (s.charAt(0) == '(') {
			return "";
		}
		return cInstruction(s);
	}
	
	public String aInstruction(String s) {
		s = s.substring(1, s.length());
		int i;
		if (isNumeric(s)) {
			i = Integer.parseInt(s);
		} else if (sT.exist(s)) {
			i = sT.get(s);
		} else {
			sT.put(s, this.n);
			i = this.n;
			this.n++;
		}
		
		s = Integer.toBinaryString(i);
		
		while (s.length() < 16) {
			s = 0 + s;
		}
		
		return s;
	}
	
	public static boolean isNumeric(String s) {
		try {
			Integer.parseInt(s);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}
	
	public String cInstruction(String s) {
		
		//COMP
		String ret = "111";
		String c ;
		if (s.contains("=")) {
			if (s.contains(";")) {
				c = s.substring(s.indexOf("=") + 1, s.indexOf(";"));
			} else c = s.substring(s.indexOf("=") + 1);
		} else if (s.contains(";")) {
			c = s.substring(0, s.indexOf(";"));
		} else c = s;
		
		ret += cT.get(c);
		
		//DEST
		if (s.contains("=")) {
			String d = s.substring(0,s.indexOf("="));
			String d1 = contains(d, "A");
			String d2 = contains(d, "D");
			String d3 = contains(d, "M");
			ret += d1 + d2 + d3; 
		} else ret += "000";
		
		//JUMP
		if (s.contains(";")) {
			String j = s.substring(s.indexOf(";") + 1);
			switch (j) {
			case "JGT":
				ret += "001";
				break;
			case "JEQ":
				ret += "010";
				break;
			case "JGE":
				ret += "011";
				break;
			case "JLT":
				ret += "100";
				break;
			case "JNE":
				ret += "101";
				break;
			case "JLE":
				ret += "110";
				break;
			case "JMP":
				ret += "111";
				break;
			}
		} else ret += "000";

		return ret;
	}
	
	public String label() {
		
		return "";
	}
	
	public String process(String s) {
		if (s.contains("//")) {
			s = s.substring(0, s.indexOf(("//")));
		}
		s = s.trim();
		return s;
	}
	
	public String contains(String s, String search) {
		if (s.contains(search)) {
			return "1";
		} else return "0";
	}
}
