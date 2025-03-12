package Symbol;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SymbolTable {
    private HashMap<String, SymbolEntry> table;
    private int currentScope;

    public SymbolTable() {
        this.table = new HashMap<>();
        this.currentScope = 0; // Global scope starts at 0
    }

    // Increase scope level when entering a new block
    public void enterScope() {
        currentScope++;
    }

    // Decrease scope level and remove all variables declared in this scope
    public void exitScope() {
        Iterator<Map.Entry<String, SymbolEntry>> iterator = table.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SymbolEntry> entry = iterator.next();
            if (entry.getValue().scope == currentScope) {
                iterator.remove();
            }
        }
        currentScope--;
    }

    // Insert new entry (return false if already declared in the same scope)
    public boolean insert(String name, int type, int dim, int offset, int pc) {
        if (table.containsKey(name) && table.get(name).scope == currentScope) {
            return false; // Duplicate declaration in the same scope
        }
        table.put(name, new SymbolEntry(type, currentScope, dim, offset, pc));
        return true;
    }

    // Lookup an identifier (only finds the closest visible scope)
    public SymbolEntry lookup(String name) {
        return table.get(name); // Directly returns the latest entry
    }

    // Print all entries for debugging
    public void printTable() {
        System.out.println("Symbol Table:");
        for (Map.Entry<String, SymbolEntry> entry : table.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}
