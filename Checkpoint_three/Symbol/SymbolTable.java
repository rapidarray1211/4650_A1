package Symbol;

import java.util.*;

public class SymbolTable {
    private final List<Map<String, SymbolEntry>> scopeStack = new ArrayList<>();
    private int currentScope;
    private final AnalyzerPrinter printer;
    private boolean preserve = true;

    public SymbolTable(AnalyzerPrinter printer) {
        this.printer = printer;
        this.currentScope = -1;
    }

    public void setPreserve(boolean preserve) {
        this.preserve = preserve;
    }

    public void enterScope(String context) {
        currentScope++;
        scopeStack.add(new HashMap<>());
        printer.indent(currentScope);
        printer.printMsg("[ENTER] Entering Scope Level: " + currentScope + (context != null ? " for " + context : ""));
    }

    public void exitScope(String context) {
        printer.indent(currentScope);
        printer.printMsg("[EXIT] Exiting Scope Level: " + currentScope);
        printTable();

        if (!preserve && !scopeStack.isEmpty()) {
            scopeStack.remove(scopeStack.size() - 1);
        }

        currentScope--;
    }

    public boolean insert(String name, int type, int dim, int offset, int pc) {
        Map<String, SymbolEntry> current = scopeStack.get(scopeStack.size() - 1);
        if (current.containsKey(name)) {
            return false;
        }
        current.put(name, new SymbolEntry(type, currentScope, dim, offset, pc));
        return true;
    }

    public boolean insert(String name, int type, int dim, int offset, int pc, List<Integer> paramTypes, List<Integer> paramDims) {
        Map<String, SymbolEntry> current = scopeStack.get(scopeStack.size() - 1);
        if (current.containsKey(name)) {
            return false;
        }
        current.put(name, new SymbolEntry(type, currentScope, dim, offset, pc, paramTypes, paramDims));
        return true;
    }

    public SymbolEntry lookup(String name) {
        for (int i = scopeStack.size() - 1; i >= 0; i--) {
            Map<String, SymbolEntry> scope = scopeStack.get(i);
            if (scope.containsKey(name)) {
                return scope.get(name);
            }
        }
        return null;
    }

    public void printTable() {
        if (currentScope == -1) return;
        printer.indent(currentScope);
        printer.printMsg("[PRINT] Symbol Table Dump (Current Scope: " + currentScope + ")");

        boolean empty = true;
        Set<String> printed = new HashSet<>();
        
        for (int i = currentScope; i >= 0; i--) {
            Map<String, SymbolEntry> scope = scopeStack.get(i);
            for (Map.Entry<String, SymbolEntry> entry : scope.entrySet()) {
                if (!printed.contains(entry.getKey())) {
                    printer.indent(currentScope);
                    printer.printMsg(entry.getKey() + " -> " + entry.getValue());
                    printed.add(entry.getKey());
                    empty = false;
                }
            }
        }
        
        if (empty) {
            printer.indent(currentScope);
            printer.printMsg("[EMPTY] Symbol Table is empty.");
        }
        
    }

    public SymbolEntry lookupGlobal(String name) {
        if (scopeStack.size() > 1) {
            Map<String, SymbolEntry> globalScope = scopeStack.get(1);
            return globalScope.get(name);
        }
        return null;
    }
}
