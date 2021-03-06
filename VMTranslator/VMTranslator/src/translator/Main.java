package translator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
	public static void main(String[] args) throws IOException {
		
		//output file
		Scanner in = new Scanner(System.in);
		System.out.println("Please enter fileName (ex: " + args[0].substring(args[0].indexOf("src/") + 4, args[0].length()-1) +".asm): ");
		String output = in.next();
		CodeWriter C = new CodeWriter(output);
		in.close();
		
		//input file/directory check ex: src/SimpleAdd.asm or src/BasicLoop
		File file = new File(args[0]);
		System.out.println(file.toString());
		ArrayList<File> files = new ArrayList<>();
		if (file.isFile() && file.getName().endsWith(".vm")) {
			files.add(file);
		} else if (file.isDirectory()) {
			File[] fileList = file.listFiles();
			for (File f : fileList) {
				System.out.println(f.toString());
				if (f.getName().endsWith(".vm")) files.add(f);
				if (f.getName().endsWith("Sys.vm")) C.writeInit();;
			}
		}
		
		//loop existing files
		System.out.println("IMPORTED FILES: ");
		for (File f : files) {
			C.setFileName(f.toString());
			System.out.println(f.toString());
			Parser input = new Parser(f.toString());
			while (input.hasNextLine()) {
				String line = input.nextLine();
				if (line.isEmpty()) {
					continue;
				}
				C.process(line);			
			}
		}
		System.out.println("Complete!");
		C.close();

		
	}
}
