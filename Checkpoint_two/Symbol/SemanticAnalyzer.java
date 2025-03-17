package Symbol;

import absyn.*;

import java.util.List;
import java.util.ArrayList;

public class SemanticAnalyzer implements AbsynVisitor {
    private SymbolTable symbolTable;
    private String currentFunctionReturnType = null;
    private boolean errorFlag = false;
    private String errorOutput = "";
	private AnalyzerPrinter printer;

    public SemanticAnalyzer(AnalyzerPrinter printer) {
        symbolTable = new SymbolTable();
		this.printer = printer;
    }
	
	/*public SemanticAnalyzer(AnalyzerPrinter printer) {
        this.printer = printer;
    }*/


    public void analyze(DecList root) {
        symbolTable.enterScope();
        visit(root, 0);
        symbolTable.exitScope(printer);
        symbolTable.printTable(printer);
        System.out.println();
        System.out.println("[END] Semantic Analysis Complete.");
        if (!errorFlag) {
            System.out.println("No semantic errors");
        }
        else {
            System.err.println(errorOutput);
        }
    }

    @Override
    public void visit(DecList node, int level) {
        //System.out.println("[VISIT] DecList at level " + level);
		//printer.printLevel("[VISIT] DecList at level " + level, level);
        while (node != null) {
            if (node.head != null) {
                node.head.accept(this, level);
            }
            node = node.tail;
        }
    }

    @Override
    public void visit(FunctionDec node, int level) {
        //System.out.println("[VISIT] FunctionDec '" + node.func_name + "' at level " + level);
		//printer.printLevel("[VISIT] FunctionDec '" + node.func_name + "' at level " + level, level);
        List<String> paramTypes = new ArrayList<>();
        switch (node.return_type.type) {
            case 0:
                currentFunctionReturnType = "bool";
            case 1:
                currentFunctionReturnType = "int";
            case 2:
                currentFunctionReturnType = "void";
            case 3:
                currentFunctionReturnType = "null";
        }

        VarDecList params = node.parameters;
        while (params != null && params.head != null) {
            paramTypes.add(getType(params.head));
            params = params.tail;
        }

        SymbolEntry existingEntry = symbolTable.lookup(node.func_name);

        if (node.body instanceof NilExp) {
            if (existingEntry != null) {
                errorFlag = true;
                errorOutput = errorOutput + "[ERROR] Function prototype for '" + node.func_name + "' is re-declared" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            } else {
                symbolTable.insert(node.func_name, node.return_type.type, paramTypes.size(), 0, 0);
                //System.out.println("[PROTOTYPE] Declared function prototype '" + node.func_name + "'");
				//printer.printLevel("[PROTOTYPE] Declared function prototype '" + node.func_name + "'", level);
            }
        } else {
            if (existingEntry != null) {
                if (existingEntry.dim != paramTypes.size()) {
                    errorFlag = true;
                    errorOutput = errorOutput + "[ERROR] Function declaration for '" + node.func_name + "' does not match prototype" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
                } else {
                    //System.out.println("[MATCH] Function '" + node.func_name + "' matches prototype.");
					//printer.printLevel("[MATCH] Function '" + node.func_name + "' matches prototype.", level);
                }
            } else {
                //system.out.println("[DEFINE] Declaring function '" + node.func_name + "'");
				//printer.printLevel("[DEFINE] Declaring function '" + node.func_name + "'", level);
                symbolTable.insert(node.func_name, node.return_type.type, paramTypes.size(), 0, 0);
            }

            // symbolTable.enterScope();
            //system.out.println("[ENTER] Scope for function '" + node.func_name + "'");
			//printer.printLevel("[ENTER] Scope for function '" + node.func_name + "'", level);
            
            params = node.parameters;
            while (params != null && params.head != null) {
                params.head.accept(this, level + 1);
                params = params.tail;
            }

            node.body.accept(this, level + 1);
            // symbolTable.exitScope();
            //system.out.println("[EXIT] Scope for function '" + node.func_name + "'");
			//printer.printLevel("[EXIT] Scope for function '" + node.func_name + "'", level);
            currentFunctionReturnType = null;
        }
    }

    @Override
    public void visit(SimpleDec node, int level) {
        //system.out.println("[VISIT] SimpleDec '" + node.name + "' at level " + level);
		//printer.printLevel("[VISIT] SimpleDec '" + node.name + "' at level " + level, level);
        boolean success = symbolTable.insert(node.name, node.type.type, 0, 0, 0);
        if (!success) {
            errorFlag = true;
            errorOutput = errorOutput + "[ERROR] Variable '" + node.name + "' is already declared in this scope " + " at line " + (node.row + 1) + " and column " + (node.col + 1);
        }
    }

    @Override
    public void visit(ArrayDec node, int level) {
        //system.out.println("[VISIT] ArrayDec '" + node.name + "' at level " + level);
		//printer.printLevel("[VISIT] ArrayDec '" + node.name + "' at level " + level, level);
        boolean success = symbolTable.insert(node.name, node.type.type, node.size, 0, 0);
        if (!success) {
            errorFlag = true;
            errorOutput = errorOutput + "[ERROR] Array '" + node.name + "' is already declared in this scope" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
        }
    }

    @Override
    public void visit(CompoundExp node, int level) {
        // System.out.println("[ENTER] Compound Statement Scope at level " + level);
		//printer.printLevel("[ENTER] Compound Statement Scope at level " + level, level);
        symbolTable.enterScope();
        
        if (node.decs != null) {
            node.decs.accept(this, level);
        }
        
        if (node.exps != null) {
            //system.out.println("[VISIT] Visiting expressions in CompoundExp at level " + level);
			//printer.printLevel("[VISIT] Visiting expressions in CompoundExp at level " + level, level);
            node.exps.accept(this, level); // Ensure expressions are visited!
        }
        
        symbolTable.exitScope(printer);
        //system.out.println("[EXIT] Compound Statement Scope at level " + level);
		//printer.printLevel("[EXIT] Compound Statement Scope at level " + level, level);
    }    

    @Override
    public void visit(AssignExp node, int level) {
        //system.out.println("[VISIT] AssignExp at level " + level);
		//printer.printLevel("[VISIT] AssignExp at level " + level, level);
    
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
                    errorOutput = errorOutput + "[ERROR] Variable '" + varName + "' is undeclared " + " at line " + (node.row + 1) + " and column " + (node.col + 1);
                    errorFlag = true;
                    return;
                } else {
                    lhsType = getTypeFromEntry(lhsEntry);
                    //system.out.println("[LOOKUP] Found variable '" + varName + "' of type '" + lhsType + "' in Scope: " + lhsEntry.scope);
					//printer.printLevel("[LOOKUP] Found variable '" + varName + "' of type '" + lhsType + "' in Scope: " + lhsEntry.scope, level);
                }
            }
        } else {
            errorOutput = errorOutput + "[ERROR] LHS of assignment is not a variable" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
            return;
        }
    
        String rhsType = getExpressionType(node.rhs);
    
        if (lhsType != null && rhsType != null && !lhsType.equals(rhsType)) {
            errorOutput = errorOutput + "[ERROR] Type mismatch in assignment: Cannot assign '" + rhsType + "' to '" + lhsType + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
    
        node.rhs.accept(this, level);
    }
    
    
    

    @Override
    public void visit(OpExp node, int level) {
        //system.out.println("[VISIT] OpExp at level " + level);
		//printer.printLevel("[VISIT] OpExp at level " + level, level);
        node.left.accept(this, level);
        node.right.accept(this, level);
        if (!isTypeCompatible(node.left, node.right)) {
            errorOutput = errorOutput + "[ERROR] Type mismatch in binary expression " + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
    }

    @Override
    public void visit(CallExp node, int level) {
        //system.out.println("[VISIT] CallExp: Calling function '" + node.func + "' at level " + level);
		//printer.printLevel("[VISIT] CallExp: Calling function '" + node.func + "' at level " + level, level);
        SymbolEntry entry = symbolTable.lookupGlobal(node.func);
        if (entry == null) {
            errorOutput = errorOutput + "[ERROR] Function '" + node.func + "' is undefined" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
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
            errorOutput = errorOutput + "[ERROR] Function '" + node.func + "' expects " + entry.dim + " arguments but got " + argCount + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
    }

    @Override
    public void visit(IfExp node, int level) {
        //system.out.println("[VISIT] IfExp");
		//printer.printLevel("[VISIT] IfExp", level);
    
        node.test.accept(this, level);
        String conditionType = getExpressionType(node.test);
    
        if (!conditionType.equals("bool")) {
            errorOutput = errorOutput + "[ERROR] Condition in if-statement must be  'bool', but got '" + conditionType + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
    
        node.thenpart.accept(this, level);
        if (node.elsepart != null) {
            node.elsepart.accept(this, level);
        }
    }
    

    @Override
    public void visit(WhileExp node, int level) {
        //system.out.println("[VISIT] WhileExp");
		//printer.printLevel("[VISIT] WhileExp", level);
        node.test.accept(this, level);
        String conditionType = getExpressionType(node.test);
    
        if (!conditionType.equals("bool")) {
            errorOutput = errorOutput + "[ERROR] Condition in if-statement must be  'bool', but got '" + conditionType + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
        node.body.accept(this, level);
    }

    @Override
    public void visit(ReturnExp node, int level) {
        //system.out.println("[VISIT] ReturnExp");
		//printer.printLevel("[VISIT] ReturnExp", level);
    
        if (currentFunctionReturnType == null) {
            errorOutput = errorOutput + "[ERROR] Return statement found outside of a function" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
            return;
        }
    
        if (node.exp == null) {
            if (!currentFunctionReturnType.equals("void")) {
                errorOutput = errorOutput + "[ERROR] Missing return value in non-void function" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
                errorFlag = true;
            }
            return;
        }
    
        node.exp.accept(this, level);
        String returnType = getExpressionType(node.exp);
    
        if (!returnType.equals(currentFunctionReturnType)) {
            errorOutput = errorOutput + "[ERROR] Function must return '" + currentFunctionReturnType + "', but got '" + returnType + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
    }
    
    
    @Override
    public void visit(VarExp node, int level) {
        //system.out.println("[VISIT] VarExp at level " + level);
		//printer.printLevel("[VISIT] VarExp at level " + level, level);
    
        if (node.variable instanceof SimpleVar) {
            String varName = ((SimpleVar) node.variable).name;
            SymbolEntry entry = symbolTable.lookup(varName);
    
            if (entry == null) {
                errorOutput = errorOutput + "[ERROR] Variable '" + varName + "' is undeclared" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
                errorFlag = true;
            } else {
                //system.out.println("[LOOKUP] Found variable '" + varName + "' in Scope: " + entry.scope);
				//printer.printLevel("[LOOKUP] Found variable '" + varName + "' in Scope: " + entry.scope, level);
            }
        } else if (node.variable instanceof IndexVar) {
            String varName = ((IndexVar) node.variable).name;
            SymbolEntry entry = symbolTable.lookup(varName);
    
            if (entry == null) {
                errorOutput = errorOutput + "[ERROR] Array '" + varName + "' is undeclared" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
                errorFlag = true;
            } else if (entry.dim <= 0) {
                errorOutput = errorOutput + "[ERROR] '" + varName + "' is not an array "+ " at line " + (node.row + 1) + " and column " + (node.col + 1);
                errorFlag = true;
            } else {
                //system.out.println("[LOOKUP] Found array '" + varName + "' in Scope: " + entry.scope);
				//printer.printLevel("[LOOKUP] Found array '" + varName + "' in Scope: " + entry.scope, level);
            }
        } else {
            errorOutput = errorOutput + "[ERROR] Unknown variable type in VarExp" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
    }
    

    public void visit(IntExp node, int level) {
        //system.out.println("[VISIT] IntExp");
		//printer.printLevel("[VISIT] IntExp", level);
		
    }

    public void visit(Exp node, int level) {
        //system.out.println("[VISIT] Exp");
		//printer.printLevel("[VISIT] Exp", level);
    }

    public void visit(BoolExp node, int level) {
        //system.out.println("[VISIT] BoolExp");
		//printer.printLevel("[VISIT] BoolExp", level);
    }

    public void visit(WriteExp node, int level) {
        //system.out.println("[VISIT] WriteExp");
		//printer.printLevel("[VISIT] WriteExp", level);
        node.output.accept(this, level);
    }

    public void visit(ReadExp node, int level) {
        //system.out.println("[VISIT] ReadExp");
		//printer.printLevel("[VISIT] ReadExp", level);
        node.input.accept(this, level);
    }

    public void visit(RepeatExp node, int level) {
        //system.out.println("[VISIT] RepeatExp");
		//printer.printLevel("[VISIT] RepeatExp", level);
        node.exps.accept(this, level);
        node.test.accept(this, level);
    }

    @Override
    public void visit(ExpList node, int level) {
        //system.out.println("[VISIT] ExpList at level " + level);
		//printer.printLevel("[VISIT] ExpList at level " + level, level);
    
        while (node != null) {
            if (node.head != null) {
                //system.out.println("[VISIT] Expression inside ExpList at level " + level);
				//printer.printLevel("[VISIT] Expression inside ExpList at level " + level, level);
                node.head.accept(this, level);
            }
            node = node.tail;
        }
    }
    

    public void visit(NilExp node, int level) {
    }

    public void visit(VarDecList node, int level) {
        //system.out.println("[VISIT] VarDecList");
		//printer.printLevel("[VISIT] VarDecList", level);
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
            errorOutput = errorOutput + "Error: Undeclared variable '" + node.name + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
    }

    public void visit(NameTy node, int level) {
        if (node.type < 0 || node.type > 3) {
            errorOutput = errorOutput + "Error: Invalid type" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
    }

    public void visit(IndexVar node, int level) {
        SymbolEntry entry = symbolTable.lookup(node.name);
        if (entry == null) {
            errorOutput = errorOutput + "Error: Undeclared array variable '" + node.name + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        } else if (entry.dim <= 0) {
            errorOutput = errorOutput + "Error: '" + node.name + "' is not an array" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
        if (node.index instanceof IntExp) {
            //system.out.println("[INFO] Valid integer index for array '" + node.name + "'.");
			//printer.printLevel("[INFO] Valid integer index for array '" + node.name + "'.", level);
        } else {
            errorOutput = errorOutput + "[ERROR] Array index for '" + node.name + "' must be an integer" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
        node.index.accept(this, level);
    }

    private boolean isTypeCompatible(Exp left, Exp right) {
        String leftType = getExpressionType(left);
        String rightType = getExpressionType(right);
    
        //system.out.println("[DEBUG] Comparing types: " + leftType + " vs " + rightType);
		printer.printMsg("[DEBUG] Comparing types: " + leftType + " vs " + rightType);
    
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
                //system.out.println("[LOOKUP] Function '" + callExp.func + "' returns type '" + getTypeFromEntry(entry) + "'");
				printer.printMsg("[LOOKUP] Function '" + callExp.func + "' returns type '" + getTypeFromEntry(entry) + "'");
                return getTypeFromEntry(entry);
            } else {
                return "unknown";
                // errorOutput = errorOutput + "[ERROR] Function '" + callExp.func + "' is undefined" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
            }
        }
        if (expr instanceof OpExp) {
            OpExp opExp = (OpExp) expr;
            String leftType = getExpressionType(opExp.left);
            String rightType = getExpressionType(opExp.right);
    
            if (!leftType.equals(rightType)) {
                // errorOutput = errorOutput + "[ERROR] Type mismatch in operation: Cannot apply operator to '" + leftType + "' and '" + rightType + " at line " + (node.row + 1) + " and column " + (node.col + 1));
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
