package semantic_analyzer;

import absyn.*;

import java.util.List;
import java.util.ArrayList;

public class SemanticAnalyzer implements AbsynVisitor {
    
    private SymbolTable symbolTable;
    private int symbolCount = 0;

    public SemanticAnalyzer(int nSymbols) {
        symbolTable = new SymbolTable(nSymbols);
    }

    //needed to implemennt this because it is in implemented class.
    public int getSymbolCount(){
        return symbolCount;
    }

    public void analyze(DecList root) {
        symbolTable.enterScope();
        visit(root, 0);
        symbolTable.exitScope();
    }

    @Override
    public void visit(DecList node, int level) {
        while (node != null) {
            if (node.head != null) {
                node.head.accept(this, level);
            }
            node = node.tail;
        }
    }

    @Override
    public void visit(FunctionDec node, int level) {
        List<String> paramTypes = new ArrayList<>();
        VarDecList params = node.parameters;
        while (params != null && params.head != null) {
            paramTypes.add(getType(params.head));
            params = params.tail;
        }
    
        SymbolEntry existingEntry = symbolTable.lookup(node.func_name);
        
        if (node.body instanceof NilExp) {
            if (existingEntry != null) {
                System.err.println("Warning: Function prototype for '" + node.func_name + "' is re-declared.");
            } else {
                symbolTable.insert(node.func_name, node.return_type.type, paramTypes.size(), 0, 0);
                System.out.println("[PROTOTYPE] Declared function prototype '" + node.func_name + "'");
            }
        } else { 
            if (existingEntry != null) {
                if (existingEntry.dim != paramTypes.size()) {
                    System.err.println("Error: Function declaration for '" + node.func_name + "' does not match prototype.");
                } else {
                    System.out.println("[MATCH] Function '" + node.func_name + "' matches prototype.");
                }
            } else {
                System.out.println("[DEFINE] Declaring function '" + node.func_name + "'");
                symbolTable.insert(node.func_name, node.return_type.type, paramTypes.size(), 0, 0);
            }
    
            symbolTable.enterScope();
            params = node.parameters;
            while (params != null && params.head != null) {
                params.head.accept(this, level + 1);
                params = params.tail;
            }
    
            node.body.accept(this, level + 1);
            symbolTable.exitScope();
        }
    }
    

    @Override
    public void visit(SimpleDec node, int level) {
        boolean success = symbolTable.insert(node.name, node.type.type, 0, 0, 0);
        if (!success) {
            System.err.println("Error: Variable '" + node.name + "' is already declared in this scope.");
        }
    }

    @Override
    public void visit(ArrayDec node, int level) {
        boolean success = symbolTable.insert(node.name, node.type.type, node.size, 0, 0);
        if (!success) {
            System.err.println("Error: Array '" + node.name + "' is already declared in this scope.");
        }
    }

    @Override
    public void visit(CompoundExp node, int level) {
        symbolTable.enterScope();
        if (node.decs != null) {
            node.decs.accept(this, level + 1);
        }
        if (node.exps != null) {
            node.exps.accept(this, level + 1);
        }
        symbolTable.exitScope();
    }

    @Override
    public void visit(AssignExp node, int level) {
        SymbolEntry lhs = symbolTable.lookup(node.lhs.toString());
        if (lhs == null) {
            System.err.println("Error: Variable '" + node.lhs + "' is undeclared.");
        }
        node.rhs.accept(this, level);
    }

    @Override
    public void visit(OpExp node, int level) {
        node.left.accept(this, level);
        node.right.accept(this, level);
        if (!isTypeCompatible(node.left, node.right)) {
            System.err.println("Error: Type mismatch in binary expression.");
        }
    }

    @Override
    public void visit(IfExp node, int level) {
        node.test.accept(this, level);
        node.thenpart.accept(this, level);
        if (node.elsepart != null) {
            node.elsepart.accept(this, level);
        }
    }

    @Override
    public void visit(WhileExp node, int level) {
        node.test.accept(this, level);
        node.body.accept(this, level);
    }

    @Override
    public void visit(ReturnExp node, int level) {
        if (node.exp != null) {
            node.exp.accept(this, level);
        }
    }
    @Override
    public void visit(CallExp node, int level) {
        SymbolEntry entry = symbolTable.lookup(node.func);
        
        if (entry == null) {
            System.err.println("Error: Function '" + node.func + "' is undefined.");
            return;
        }
    
        int argCount = 0;
        ExpList args = node.args;
        while (args != null && args.head != null) {
            args.head.accept(this, level);
            args = args.tail;
            argCount++;
        }
    
        if (entry.dim != argCount) {
            System.err.println("Error: Function '" + node.func + "' expects " + entry.dim + " arguments but got " + argCount + ".");
        }
    }
    

    @Override
    public void visit(VarExp node, int level) {
        SymbolEntry entry = symbolTable.lookup(node.variable.toString());
        if (entry == null) {
            System.err.println("Error: Variable '" + node.variable + "' is undeclared.");
        }
    }

    public void visit(IntExp node, int level) {
    }

    public void visit(BoolExp node, int level) {
    }

    public void visit(WriteExp node, int level) {
        node.output.accept(this, level);
    }

    public void visit(ReadExp node, int level) {
        node.input.accept(this, level);
    }

    public void visit(RepeatExp node, int level) {
        node.exps.accept(this, level);
        node.test.accept(this, level);
    }

    public void visit(ExpList node, int level) {
        while (node != null && node.head != null) {
            node.head.accept(this, level);
            node = node.tail;
        }
    }

    public void visit(NilExp node, int level) {
    }

    public void visit(VarDecList node, int level) {
        while (node != null) {
            if (node.head != null) {
                node.head.accept(this, level);
            }
            node = node.tail;
        }
    }

    public void visit(SimpleVar node, int level) {
        SymbolEntry entry = symbolTable.lookup(node.name);
        if (entry == null) {
            System.err.println("Error: Undeclared variable '" + node.name + "'.");
        }
    }

    public void visit(NameTy node, int level) {
        if (node.type < 0 || node.type > 3) {
            System.err.println("Error: Invalid type.");
        }
    }

    public void visit(IndexVar node, int level) {
        SymbolEntry entry = symbolTable.lookup(node.name);
        if (entry == null) {
            System.err.println("Error: Undeclared array variable '" + node.name + "'.");
        } else if (entry.dim <= 0) {
            System.err.println("Error: '" + node.name + "' is not an array.");
        }
        node.index.accept(this, level);
    }

    private boolean isTypeCompatible(Exp left, Exp right) {
        return left.getClass() == right.getClass();
    }

    private String getType(VarDec node) {
        if (node instanceof SimpleDec) return ((SimpleDec) node).type.toString();
        if (node instanceof ArrayDec) return ((ArrayDec) node).type.toString();
        return "unknown";
    }
}
