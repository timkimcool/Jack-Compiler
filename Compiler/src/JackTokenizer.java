import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class JackTokenizer {
	File file;
	Map<String, String> element;
	Scanner scan;
	String curToken;
	String curStr;
	boolean comment;


	
	public JackTokenizer(File file) {
		element = new HashMap<String, String>();
		buildMap();
		try {
			scan = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		curStr = "";
		curToken = "";
		comment = false;
		
	}
	
	public String writeXML() {
		return markUp(tokenType(), curToken);
	}
	
	public void close() {
		scan.close();
	}
	
	public boolean hasMoreTokens() {
		if (curStr.isEmpty()) {
			return scan.hasNextLine();
		}
		
		return true;
	}
	
	public String curToken() {
		return curToken;
	}
	// Get the next token from input and make it current token 
	public void advance() {
		String build = "";  // build string 1 char at a time
		curToken = "";
		
		
		// return if no more strings
		curStr = curStr.trim();
		if (!hasMoreTokens()) {  return; }
		
		// remove comments/white space
		while ((curStr.isEmpty() && scan.hasNextLine())) { 
			curStr = process(scan.nextLine()); 
		} 
		if (curStr.isEmpty()) { return; };
		
		int i = 0;
		String nextC = "";
		while (curToken.isEmpty()) {
			if (curStr.length() == 1) {
				nextC = curStr;
			} else {
				nextC = curStr.substring(i, i + 1);
				
			}
			
			if (nextC.isEmpty()) {
				i++;
				continue;
			}
			if (element.containsKey(nextC) && (element.get(nextC).equals("symbol")) && build.isEmpty()) {
				curToken = nextC;
				curStr = curStr.substring(i + 1);
			} else if (nextC.equals("\"") && build.isEmpty()) {
				curStr = curStr.substring(curStr.indexOf("\"") + 1);
				i = curStr.indexOf("\"");
				curToken = "\"" + curStr.substring(0, i);
				i++;
			} else if (nextC.equals(" ") || (element.containsKey(nextC) && element.get(nextC).equals("symbol"))) {
				curToken = build;
			} else {
				build = build.concat(nextC);
				i++;
			}
		}
		
		curStr = curStr.substring(i);
		
		switch (curToken) {
			case "<":
				curToken = "&lt;";
				break;
			case ">":
				curToken = "&gt;";
				break;
			case "\"":
				curToken = "&quot;";
				break;
			case "&":
				curToken = "&amp;";
				break;
		}
		
		return;
		
	}
	
	public String tokenType() {
		if (curToken.isEmpty()) { return ""; }
		if (element.containsKey(curToken)) {
			return element.get(curToken);
		} else if (curToken.substring(0, 1).matches("[0-9]")) {
			return "integerConstant";
		} else if (curToken.substring(0, 1).equals("\"")) {
			return "stringConstant";
		}
		return "identifier";
	}
	
	public void buildMap() {
		// keyword
		element.put("class", "keyword");
		element.put("constructor", "keyword");
		element.put("function", "keyword");
		element.put("method", "keyword");
		element.put("field", "keyword");
		element.put("static", "keyword");
		element.put("var", "keyword");
		element.put("int", "keyword");
		element.put("char", "keyword");
		element.put("boolean", "keyword");
		element.put("void", "keyword");
		element.put("true", "keyword");
		element.put("false", "keyword");
		element.put("null", "keyword");
		element.put("this", "keyword");
		element.put("let", "keyword");
		element.put("do", "keyword");
		element.put("if", "keyword");
		element.put("else", "keyword");
		element.put("while", "keyword");
		element.put("return", "keyword");
		
		// symbol
		element.put("{", "symbol");
		element.put("}", "symbol");
		element.put("(", "symbol");
		element.put(")", "symbol");
		element.put("[", "symbol");
		element.put("]", "symbol");
		element.put(".", "symbol");
		element.put(",", "symbol");
		element.put(";", "symbol");
		element.put("+", "symbol");
		element.put("-", "symbol");
		element.put("*", "symbol");
		element.put("/", "symbol");
		element.put("&amp;", "symbol");
		element.put("|", "symbol");
		element.put("&lt;", "symbol");
		element.put("&gt;", "symbol");
		element.put("=", "symbol");
		element.put("~", "symbol");
	}
	
	public String process(String s) {
		if (s.contains("//")) {
			s = s.substring(0, s.indexOf(("//")));
		}
		if (s.contains("/*")) {
			comment = true;
		}
		if (comment) {
			if (s.contains("*/")) {
				if (s.indexOf("*/") + 2 == s.length()) {
					s = "";
				} else {
					s = s.substring(s.indexOf(("*/") + 1));
				}
				comment = false;
			}
			else { s = ""; }
		}
		return s.trim();
	}
	
	public String markUp(String keyword, String value) {
		if (value.contains("\"")) {
			value = value.substring(1, value.length());
		}
		return "<" + keyword + ">" + " " + value + " </" + keyword + ">";
	}
}
