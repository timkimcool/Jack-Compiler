package assembler;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
	private Map<String, Integer> sTable;
	
	public SymbolTable() {
		sTable = new HashMap<>();
		
		//pre-defined symbols
		for (int i = 0; i < 16; i++) {
			sTable.put("R" + i, i);
		}
		sTable.put("SCREEN", 16384);
		sTable.put("KBD", 24576);
		sTable.put("SP", 0);
		sTable.put("LCL", 1);
		sTable.put("ARG", 2);
		sTable.put("THIS", 3);
		sTable.put("THAT", 4);
	}
	
	public void put(String s, Integer i) {
		sTable.put(s, i);
	}
	
	public boolean exist(String s) {
		return sTable.containsKey(s);
	}
	
	public Integer get(String s) {
		return sTable.get(s);
	}
}
