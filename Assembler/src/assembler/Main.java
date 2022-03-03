package assembler;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
	public static void main(String[] args) throws IOException {

		Parser first = new Parser("src/assembler/Pong.asm");
		Parser second = new Parser("src/assembler/Pong.asm");
		FileWriter print = new FileWriter("src/assembler/Pong.hack");
		Translator T = new Translator();
		T.firstPass(first);
		
		while (second.hasNextLine()) {
			String line = second.nextLine();
			line = T.process(line);
			if (line.isEmpty()) continue;
			String write = T.Translate(line);
			if (write.isEmpty()) continue;
			print.write(write + "\n");
		}
		print.close();
	}
}
