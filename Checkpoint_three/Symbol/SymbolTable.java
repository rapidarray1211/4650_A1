package Symbol;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;

public class SymbolTable {
    private HashMap<String, SymbolEntry> table;
    private int currentScope;
    private AnalyzerPrinter printer;
    private boolean preserve = true;

    public SymbolTable(AnalyzerPrinter printer) {
        this.table = new HashMap<>();
        this.printer = printer;
        this.currentScope = -1;
    }

    public void setPreserve(boolean preserve) {
        this.preserve = preserve;
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

        if (!preserve) {
            Iterator<Map.Entry<String, SymbolEntry>> iterator = table.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, SymbolEntry> entry = iterator.next();
                if (entry.getValue().scope == currentScope) {
                    iterator.remove();
                }
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

    public boolean insert(String name, int type, int dim, int offset, int pc, List<Integer> paramTypes, List<Integer> paramDims) {
        if (table.containsKey(name) && table.get(name).scope == currentScope) {
            return false;
        }
        table.put(name, new SymbolEntry(type, currentScope, dim, offset, pc, paramTypes, paramDims));
        return true;
    }

    public SymbolEntry lookup(String name) {
        return table.get(name);
    }

    public void printTable() {
        if (currentScope == -1) return;
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
