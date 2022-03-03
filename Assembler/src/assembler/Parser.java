package assembler;

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
		return scan.nextLine();
	}
	
	public boolean hasNextLine() {
		return scan.hasNextLine();
	}
}
