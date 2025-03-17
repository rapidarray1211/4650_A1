package Symbol;

import absyn.*;

import java.util.List;
import java.util.ArrayList;

public class SemanticAnalyzer implements AbsynVisitor {
    private SymbolTable symbolTable;

    public SemanticAnalyzer() {
        symbolTable = new SymbolTable();
    }


    public void analyze(DecList root) {
        System.out.println("\n[START] Semantic Analysis...");
        symbolTable.enterScope();
        visit(root, 0);
        symbolTable.exitScope();
        System.out.println("[END] Semantic Analysis Complete.");
    }

    @Override
    public void visit(DecList node, int level) {
        System.out.println("[VISIT] DecList at level " + level);
        while (node != null) {
            if (node.head != null) {
                node.head.accept(this, level);
            }
            node = node.tail;
        }
    }

    @Override
    public void visit(FunctionDec node, int level) {
        System.out.println("[VISIT] FunctionDec '" + node.func_name + "' at level " + level);
        List<String> paramTypes = new ArrayList<>();
        VarDecList params = node.parameters;
        while (params != null && params.head != null) {
            paramTypes.add(getType(params.head));
            params = params.tail;
        }

        SymbolEntry existingEntry = symbolTable.lookup(node.func_name);

        if (node.body instanceof NilExp) {
            if (existingEntry != null) {
                System.err.println("[ERROR] Function prototype for '" + node.func_name + "' is re-declared" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
            } else {
                symbolTable.insert(node.func_name, node.return_type.type, paramTypes.size(), 0, 0);
                System.out.println("[PROTOTYPE] Declared function prototype '" + node.func_name + "'");
            }
        } else {
            if (existingEntry != null) {
                if (existingEntry.dim != paramTypes.size()) {
                    System.err.println("[ERROR] Function declaration for '" + node.func_name + "' does not match prototype" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
                } else {
                    System.out.println("[MATCH] Function '" + node.func_name + "' matches prototype.");
                }
            } else {
                System.out.println("[DEFINE] Declaring function '" + node.func_name + "'");
                symbolTable.insert(node.func_name, node.return_type.type, paramTypes.size(), 0, 0);
            }

            symbolTable.enterScope();
            System.out.println("[ENTER] Scope for function '" + node.func_name + "'");
            
            params = node.parameters;
            while (params != null && params.head != null) {
                params.head.accept(this, level + 1);
                params = params.tail;
            }

            node.body.accept(this, level + 1);
            symbolTable.exitScope();
            System.out.println("[EXIT] Scope for function '" + node.func_name + "'");
        }
    }

    @Override
    public void visit(SimpleDec node, int level) {
        System.out.println("[VISIT] SimpleDec '" + node.name + "' at level " + level);
        boolean success = symbolTable.insert(node.name, node.type.type, 0, 0, 0);
        if (!success) {
            System.err.println("[ERROR] Variable '" + node.name + "' is already declared in this scope " + " at line " + (node.row + 1) + " and column " + (node.col + 1));
        }
    }

    @Override
    public void visit(ArrayDec node, int level) {
        System.out.println("[VISIT] ArrayDec '" + node.name + "' at level " + level);
        boolean success = symbolTable.insert(node.name, node.type.type, node.size, 0, 0);
        if (!success) {
            System.err.println("[ERROR] Array '" + node.name + "' is already declared in this scope" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
        }
    }

    @Override
    public void visit(CompoundExp node, int level) {
        // System.out.println("[ENTER] Compound Statement Scope at level " + level);
        // symbolTable.enterScope();
        
        if (node.decs != null) {
            node.decs.accept(this, level);
        }
        
        if (node.exps != null) {
            System.out.println("[VISIT] Visiting expressions in CompoundExp at level " + level);
            node.exps.accept(this, level); // Ensure expressions are visited!
        }
        
        // symbolTable.exitScope();
        System.out.println("[EXIT] Compound Statement Scope at level " + level);
    }    

    @Override
    public void visit(AssignExp node, int level) {
        System.out.println("[VISIT] AssignExp at level " + level);
    
        String lhsType = null;
        String varName = null;
    
        if (node.lhs instanceof VarExp) {
            VarExp varExp = (VarExp) node.lhs;
    
            if (varExp.variable instanceof SimpleVar) {
                varName = ((SimpleVar) varExp.variable).name;
            } else if (varExp.variable instanceof IndexVar) {
                varName = ((IndexVar) varExp.variable).name;
            }
    
            if (varName != null) {
                SymbolEntry lhsEntry = symbolTable.lookup(varName);
    
                if (lhsEntry == null) {
                    System.err.println("[ERROR] Variable '" + varName + "' is undeclared " + " at line " + (node.row + 1) + " and column " + (node.col + 1));
                    return;
                } else {
                    lhsType = getTypeFromEntry(lhsEntry);
                    System.out.println("[LOOKUP] Found variable '" + varName + "' of type '" + lhsType + "' in Scope: " + lhsEntry.scope);
                }
            }
        } else {
            System.err.println("[ERROR] LHS of assignment is not a variable" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
            return;
        }
    
        String rhsType = getExpressionType(node.rhs);
    
        if (lhsType != null && rhsType != null && !lhsType.equals(rhsType)) {
            System.err.println("[ERROR] Type mismatch in assignment: Cannot assign '" + rhsType + "' to '" + lhsType + " at line " + (node.row + 1) + " and column " + (node.col + 1) );
        }
    
        node.rhs.accept(this, level);
    }
    
    
    

    @Override
    public void visit(OpExp node, int level) {
        System.out.println("[VISIT] OpExp at level " + level);
        node.left.accept(this, level);
        node.right.accept(this, level);
        if (!isTypeCompatible(node.left, node.right)) {
            System.err.println("[ERROR] Type mismatch in binary expression " + " at line " + (node.row + 1) + " and column " + (node.col + 1));
        }
    }

    @Override
    public void visit(CallExp node, int level) {
        System.out.println("[VISIT] CallExp: Calling function '" + node.func + "' at level " + level);
        SymbolEntry entry = symbolTable.lookupGlobal(node.func);
        if (entry == null) {
            System.err.println("[ERROR] Function '" + node.func + "' is undefined" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
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
            System.err.println("[ERROR] Function '" + node.func + "' expects " + entry.dim + " arguments but got " + argCount + " at line " + (node.row + 1) + " and column " + (node.col + 1));
        }
    }

    @Override
    public void visit(IfExp node, int level) {
        System.out.println("[VISIT] IfExp");
    
        node.test.accept(this, level);
        String conditionType = getExpressionType(node.test);
    
        if (!conditionType.equals("bool")) {
            System.err.println("[ERROR] Condition in if-statement must be  'bool', but got '" + conditionType + " at line " + (node.row + 1) + " and column " + (node.col + 1));
        }
    
        node.thenpart.accept(this, level);
        if (node.elsepart != null) {
            node.elsepart.accept(this, level);
        }
    }
    

    @Override
    public void visit(WhileExp node, int level) {
        System.out.println("[VISIT] WhileExp");
        node.test.accept(this, level);
        String conditionType = getExpressionType(node.test);
    
        if (!conditionType.equals("bool")) {
            System.err.println("[ERROR] Condition in if-statement must be  'bool', but got '" + conditionType + " at line " + (node.row + 1) + " and column " + (node.col + 1));
        }
        node.body.accept(this, level);
    }

    @Override
    public void visit(ReturnExp node, int level) {
        System.out.println("[VISIT] ReturnExp");
        if (node.exp != null) {
            node.exp.accept(this, level);
        }
    }
    
    @Override
    public void visit(VarExp node, int level) {
        System.out.println("[VISIT] VarExp at level " + level);
    
        if (node.variable instanceof SimpleVar) {
            String varName = ((SimpleVar) node.variable).name;
            SymbolEntry entry = symbolTable.lookup(varName);
    
            if (entry == null) {
                System.err.println("[ERROR] Variable '" + varName + "' is undeclared" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
            } else {
                System.out.println("[LOOKUP] Found variable '" + varName + "' in Scope: " + entry.scope);
            }
        } else if (node.variable instanceof IndexVar) {
            String varName = ((IndexVar) node.variable).name;
            SymbolEntry entry = symbolTable.lookup(varName);
    
            if (entry == null) {
                System.err.println("[ERROR] Array '" + varName + "' is undeclared" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
            } else if (entry.dim <= 0) {
                System.err.println("[ERROR] '" + varName + "' is not an array "+ " at line " + (node.row + 1) + " and column " + (node.col + 1));
            } else {
                System.out.println("[LOOKUP] Found array '" + varName + "' in Scope: " + entry.scope);
            }
        } else {
            System.err.println("[ERROR] Unknown variable type in VarExp" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
        }
    }
    

    public void visit(IntExp node, int level) {
        System.out.println("[VISIT] IntExp");
    }

    public void visit(Exp node, int level) {
        System.out.println("[VISIT] Exp");
    }

    public void visit(BoolExp node, int level) {
        System.out.println("[VISIT] BoolExp");
    }

    public void visit(WriteExp node, int level) {
        System.out.println("[VISIT] WriteExp");
        node.output.accept(this, level);
    }

    public void visit(ReadExp node, int level) {
        System.out.println("[VISIT] ReadExp");
        node.input.accept(this, level);
    }

    public void visit(RepeatExp node, int level) {
        System.out.println("[VISIT] RepeatExp");
        node.exps.accept(this, level);
        node.test.accept(this, level);
    }

    @Override
    public void visit(ExpList node, int level) {
        System.out.println("[VISIT] ExpList at level " + level);
    
        while (node != null) {
            if (node.head != null) {
                System.out.println("[VISIT] Expression inside ExpList at level " + level);
                node.head.accept(this, level);
            }
            node = node.tail;
        }
    }
    

    public void visit(NilExp node, int level) {
    }

    public void visit(VarDecList node, int level) {
        System.out.println("[VISIT] VarDecList");
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
            System.err.println("Error: Undeclared variable '" + node.name + " at line " + (node.row + 1) + " and column " + (node.col + 1));
        }
    }

    public void visit(NameTy node, int level) {
        if (node.type < 0 || node.type > 3) {
            System.err.println("Error: Invalid type" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
        }
    }

    public void visit(IndexVar node, int level) {
        SymbolEntry entry = symbolTable.lookup(node.name);
        if (entry == null) {
            System.err.println("Error: Undeclared array variable '" + node.name + " at line " + (node.row + 1) + " and column " + (node.col + 1));
        } else if (entry.dim <= 0) {
            System.err.println("Error: '" + node.name + "' is not an array" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
        }
        if (node.index instanceof IntExp) {
            System.out.println("[INFO] Valid integer index for array '" + node.name + "'.");
        } else {
            System.err.println("[ERROR] Array index for '" + node.name + "' must be an integer" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
        }
        node.index.accept(this, level);
    }

    private boolean isTypeCompatible(Exp left, Exp right) {
        String leftType = getExpressionType(left);
        String rightType = getExpressionType(right);
    
        System.out.println("[DEBUG] Comparing types: " + leftType + " vs " + rightType);
    
        return leftType.equals(rightType);
    }
    

    private String getType(VarDec node) {
        if (node instanceof SimpleDec) return ((SimpleDec) node).type.toString();
        if (node instanceof ArrayDec) return ((ArrayDec) node).type.toString();
        return "unknown";
    }

    private String getTypeFromEntry(SymbolEntry entry) {
        if (entry.type == 0) return "bool";
        if (entry.type == 1) return "int";
        if (entry.type == 2) return "void";
        return "unknown";
    }

    private String getExpressionType(Exp expr) {
        if (expr instanceof BoolExp) return "bool";
        if (expr instanceof IntExp) return "int";
        if (expr instanceof VarExp) {
            VarExp varExp = (VarExp) expr;
            if (varExp.variable instanceof SimpleVar) {
                String varName = ((SimpleVar) varExp.variable).name;
                SymbolEntry entry = symbolTable.lookup(varName);
                if (entry != null) {
                    return getTypeFromEntry(entry);
                }
            }
        }
        if (expr instanceof CallExp) {
            CallExp callExp = (CallExp) expr;
            SymbolEntry entry = symbolTable.lookup(callExp.func);
            
            if (entry == null) {
                entry = symbolTable.lookupGlobal(callExp.func);
            }
    
            if (entry != null) {
                System.out.println("[LOOKUP] Function '" + callExp.func + "' returns type '" + getTypeFromEntry(entry) + "'");
                return getTypeFromEntry(entry);
            } else {
                return "unknown";
                // System.err.println("[ERROR] Function '" + callExp.func + "' is undefined" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
            }
        }
        if (expr instanceof OpExp) {
            OpExp opExp = (OpExp) expr;
            String leftType = getExpressionType(opExp.left);
            String rightType = getExpressionType(opExp.right);
    
            if (!leftType.equals(rightType)) {
                // System.err.println("[ERROR] Type mismatch in operation: Cannot apply operator to '" + leftType + "' and '" + rightType + " at line " + (node.row + 1) + " and column " + (node.col + 1));
                return "unknown";
            }
    
            if (opExp.op == OpExp.EQ || opExp.op == OpExp.LT || opExp.op == OpExp.GT) {
                return "bool";
            }
    
            return leftType; 
        }
        return "unknown";
    }
    
}
