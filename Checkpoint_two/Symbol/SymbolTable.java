package Symbol;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class SymbolTable {
    private HashMap<String, SymbolEntry> table;
    private int currentScope;

    public SymbolTable() {
        this.table = new HashMap<>();
        this.currentScope = 0;
    }

    public void enterScope() {
        currentScope++;
    }

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

    public boolean insert(String name, int type, int dim, int offset, int pc) {
        if (table.containsKey(name) && table.get(name).scope == currentScope) {
            return false;
        }
        table.put(name, new SymbolEntry(type, currentScope, dim, offset, pc));
        return true;
    }

    public SymbolEntry lookup(String name) {
        return table.get(name);
    }

    public void printTable() {
        System.out.println("Symbol Table:");
        for (Map.Entry<String, SymbolEntry> entry : table.entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}
