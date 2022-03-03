package translator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class Parser {
	private Scanner scan;
	private File file;
	
	public Parser(String filePath) throws FileNotFoundException {
		file = new File(filePath);
		scan = new Scanner(file);
	}
	
	public String nextLine() {
		String ret = process(scan.nextLine());
		return ret;
	}
	
	public boolean hasNextLine() {
		return scan.hasNextLine();
	}
	
	public String process(String s) {
		if (s.contains("//")) {
			s = s.substring(0, s.indexOf(("//")));
		}
		s = s.trim();
		return s;
	}
}
