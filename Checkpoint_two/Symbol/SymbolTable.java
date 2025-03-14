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
        System.out.println("[INIT] Symbol Table Created. Global Scope: " + currentScope);
    }

    public void enterScope() {
        currentScope++;
        System.out.println("\n[ENTER] Entering Scope Level: " + currentScope);
    }

    public void exitScope() {
        System.out.println("\n[EXIT] Exiting Scope Level: " + currentScope);
        printTable();
        Iterator<Map.Entry<String, SymbolEntry>> iterator = table.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SymbolEntry> entry = iterator.next();
            if (entry.getValue().scope == currentScope) {
                System.out.println("[REMOVE] Removing: " + entry.getKey() + " from Scope: " + currentScope);
                iterator.remove();
            }
        }
        currentScope--;
        System.out.println("[SCOPE UPDATE] Current Scope after exit: " + currentScope);
    }

    public boolean insert(String name, int type, int dim, int offset, int pc) {
        System.out.println("[INSERT] Attempting to insert '" + name + "' at Scope: " + currentScope);
        if (table.containsKey(name) && table.get(name).scope == currentScope) {
            System.out.println("[ERROR] Duplicate Declaration: '" + name + "' already exists in Scope: " + currentScope);
            return false;
        }
        table.put(name, new SymbolEntry(type, currentScope, dim, offset, pc));
        System.out.println("[SUCCESS] Inserted '" + name + "' (Type: " + type + ", Dim: " + dim + ", Offset: " + offset + ", PC: " + pc + ") in Scope: " + currentScope);
        return true;
    }

    public SymbolEntry lookup(String name) {
        SymbolEntry entry = table.get(name);
        if (entry != null) {
            System.out.println("[LOOKUP] Found '" + name + "' in Scope: " + entry.scope);
        } else {
            System.out.println("[LOOKUP] '" + name + "' NOT FOUND in any active scope.");
        }
        return entry;
    }

    public void printTable() {
        System.out.println("\n[PRINT] Symbol Table Dump (Current Scope: " + currentScope + ")");
        if (table.isEmpty()) {
            System.out.println("[EMPTY] Symbol Table is empty.");
        } else {
            for (Map.Entry<String, SymbolEntry> entry : table.entrySet()) {
                System.out.println(entry.getKey() + " -> " + entry.getValue());
            }
        }
    }
}
