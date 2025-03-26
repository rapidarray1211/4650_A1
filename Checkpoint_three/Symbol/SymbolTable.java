package Symbol;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

public class SymbolTable {
    private HashMap<String, SymbolEntry> table;
    private int currentScope;
    private AnalyzerPrinter printer;

    public SymbolTable(AnalyzerPrinter printer) {
        this.table = new HashMap<>();
        this.printer = printer;
        this.currentScope = -1;

    }

    public void enterScope(String context) {
        currentScope++;
        printer.indent(currentScope);
        printer.printMsg("[ENTER] Entering Scope Level: " + currentScope + (context != null ? " for " + context : ""));
    }

    public void exitScope(String context) {
        printer.indent(currentScope);
        printer.printMsg("[EXIT] Exiting Scope Level: " + currentScope);
        printTable();
        Iterator<Map.Entry<String, SymbolEntry>> iterator = table.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, SymbolEntry> entry = iterator.next();
            if (entry.getValue().scope == currentScope) {
                // System.out.println("[REMOVE] Removing: " + entry.getKey() + " from Scope: " + currentScope);
                iterator.remove();
            }
        }
        currentScope--;
        // System.out.println("[SCOPE UPDATE] Current Scope after exit: " + currentScope);
    }

    public boolean insert(String name, int type, int dim, int offset, int pc) {
        // System.out.println("[INSERT] Attempting to insert '" + name + "' at Scope: " + currentScope);
        if (table.containsKey(name) && table.get(name).scope == currentScope) {
            //System.out.println("[ERROR] Duplicate Declaration: '" + name + "' already exists in Scope: " + currentScope);
            return false;
        }
        table.put(name, new SymbolEntry(type, currentScope, dim, offset, pc));
        //System.out.println("[SUCCESS] Inserted '" + name + "' (Type: " + type + ", Dim: " + dim + ", Offset: " + offset + ", PC: " + pc + ") in Scope: " + currentScope);
        return true;
    }

    public boolean insert(String name, int type, int dim, int offset, int pc, List<Integer> paramTypes, List<Integer> paramDims) {
        // System.out.println("[INSERT] Attempting to insert '" + name + "' at Scope: " + currentScope);
        if (table.containsKey(name) && table.get(name).scope == currentScope) {
            //System.out.println("[ERROR] Duplicate Declaration: '" + name + "' already exists in Scope: " + currentScope);
            return false;
        }
        table.put(name, new SymbolEntry(type, currentScope, dim, offset, pc, paramTypes, paramDims));
        //System.out.println("[SUCCESS] Inserted '" + name + "' (Type: " + type + ", Dim: " + dim + ", Offset: " + offset + ", PC: " + pc + ") in Scope: " + currentScope);
        return true;
    }

    public SymbolEntry lookup(String name) {
        SymbolEntry entry = table.get(name);
        if (entry != null) {
            // System.out.println("[LOOKUP] Found '" + name + "' in Scope: " + entry.scope);
        } else {
            // System.out.println("[LOOKUP] '" + name + "' NOT FOUND in any active scope.");
        }
        return entry;
    }

    public void printTable() {
        if (currentScope == -1) {
            return;
        }
        printer.indent(currentScope);
        printer.printMsg("[PRINT] Symbol Table Dump (Current Scope: " + currentScope + ")");
        if (table.isEmpty()) {
            printer.indent(currentScope);
            printer.printMsg("[EMPTY] Symbol Table is empty.");
        } else {
            for (Map.Entry<String, SymbolEntry> entry : table.entrySet()) {
                if (entry.getValue().scope <= currentScope) {
                    printer.indent(currentScope);
                    printer.printMsg(entry.getKey() + " -> " + entry.getValue());
                }
            }            
        }
    }

    public SymbolEntry lookupGlobal(String name) {
        for (Map.Entry<String, SymbolEntry> entry : table.entrySet()) {
            if (entry.getKey().equals(name) && entry.getValue().scope == 1) {
                return entry.getValue();
            }
        }
        return null;
    }
    
}
