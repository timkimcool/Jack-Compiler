import java.util.HashMap;

public class SymbolTable {
	HashMap<String, String> table;			// <name, type_kind_varCount>
	int varIndex, argIndex, staticIndex, fieldIndex;
	
	public SymbolTable() {
		table = new HashMap<String, String>();
		varIndex = -1;
		argIndex = -1;
		staticIndex = -1;
		fieldIndex = -1;
		
	}
	
	public void startSubroutine() {
		table.clear();
		varIndex = -1;
		argIndex = -1;
		staticIndex = -1;
		fieldIndex = -1;
	}
	
	public void define(String kind, String type, String name) {
		switch (kind.toUpperCase()) {
			case "STATIC":
				staticIndex++;
				break;
			case "FIELD":
				kind = "this";
				fieldIndex++;
				break;
			case "ARGUMENT":
				argIndex++;
				break;
			case "LOCAL":
				varIndex++;
				break;
		}
		table.put(name, type + "_" + kind + "_" + varCount(kind));		
	}
	
	public int varCount(String kind) {
		switch (kind.toUpperCase()) {
			case "STATIC":
				return staticIndex;
			case "THIS":
				return fieldIndex;
			case "ARGUMENT":
				return argIndex;
			case "LOCAL":
				return varIndex;
		}
		
		return 0;
	}
		
	public String KindOf(String name) {
		String[] parts = table.get(name).split("_");
		return parts[1];
	}
	
	public String TypeOf(String name) {
		String[] parts = table.get(name).split("_");
		return parts[0];
	}
	
	public int IndexOf(String name) {
		String[] parts = table.get(name).split("_");
		return Integer.parseInt(parts[2]);
	}
}
