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
    private boolean hasReturn = false;
    private boolean mainDeclared = false;
    private boolean firstCompoundInFunction = false;

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
        symbolTable.exitScope();
        symbolTable.printTable();
        System.out.println();
        System.out.println("[END] Semantic Analysis Complete.");
        if (!mainDeclared) {
            errorFlag = true;
            errorOutput = errorOutput + "\n[ERROR] Missing main";
        }
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
        List<Integer> paramTypes = new ArrayList<>();
        List<Integer> paramDims = new ArrayList<>();
        firstCompoundInFunction = true;
        hasReturn = false;
        switch (node.return_type.type) {
            case 0:
                currentFunctionReturnType = "bool";
                break;
            case 1:
                currentFunctionReturnType = "int";
                break;
            case 2:
                currentFunctionReturnType = "void";
                break;
            default:
                currentFunctionReturnType = "null";
        }

        if (mainDeclared) {
            errorFlag = true;
            errorOutput = errorOutput + "\n[ERROR] Function '" + node.func_name + "' is declared after main when main must be the last function " + " at line " + (node.row + 1) + " and column " + (node.col + 1);
        }

        if (node.func_name.equals("main")) {
            mainDeclared = true;
        }

        VarDecList params = node.parameters;
        while (params != null && params.head != null) {
            if (params.head instanceof SimpleDec) {
                paramTypes.add(((SimpleDec) params.head).type.type); // Store type
                paramDims.add(0); // Simple variables have dimension 0
            } else if (params.head instanceof ArrayDec) {
                paramTypes.add(((ArrayDec) params.head).type.type); // Store type
                if (((ArrayDec) params.head).size == 0) {
                    paramDims.add(1);
                }
                else {
                    paramDims.add(((ArrayDec) params.head).size); // Store array size
                }
            } else {
                errorFlag = true;
                errorOutput = errorOutput + "\n[ERROR] Unknown parameter type at line " + (params.head.row + 1) + " and column " + (params.head.col + 1);
            }
        
            params = params.tail;
        }

        SymbolEntry existingEntry = symbolTable.lookup(node.func_name);

        if (node.body instanceof NilExp) {
            if (existingEntry != null) {
                errorFlag = true;
                errorOutput = errorOutput + "\n[ERROR] Function prototype for '" + node.func_name + "' is re-declared" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            } else {
                if (!symbolTable.insert(node.func_name, node.return_type.type, paramTypes.size(), 0, 0, paramTypes, paramDims)) {
                    errorFlag = true;
                    errorOutput = errorOutput + "\n[ERROR] Duplicate function declaration for '" + node.func_name + "' at line " + (node.row + 1) + " and column " + (node.col + 1);    
                }
                //System.out.println("[PROTOTYPE] Declared function prototype '" + node.func_name + "'");
				//printer.printLevel("[PROTOTYPE] Declared function prototype '" + node.func_name + "'", level);
            }
        } else {
            if (existingEntry != null) {
                if (existingEntry.dim != paramTypes.size()) {
                    errorFlag = true;
                    errorOutput = errorOutput + "\n[ERROR] Function declaration for '" + node.func_name + "' does not match prototype" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
                } else {
                    //System.out.println("[MATCH] Function '" + node.func_name + "' matches prototype.");
					//printer.printLevel("[MATCH] Function '" + node.func_name + "' matches prototype.", level);
                }
            } else {
                //system.out.println("[DEFINE] Declaring function '" + node.func_name + "'");
				//printer.printLevel("[DEFINE] Declaring function '" + node.func_name + "'", level);
                if (!symbolTable.insert(node.func_name, node.return_type.type, paramTypes.size(), 0, 0, paramTypes, paramDims)) {
                    errorFlag = true;
                    errorOutput = errorOutput + "\n[ERROR] Duplicate function declaration for '" + node.func_name + "' at line " + (node.row + 1) + " and column " + (node.col + 1);    
                }
            }
            symbolTable.enterScope();
            //system.out.println("[ENTER] Scope for function '" + node.func_name + "'");
			//printer.printLevel("[ENTER] Scope for function '" + node.func_name + "'", level);
            
            params = node.parameters;
            while (params != null && params.head != null) {
                params.head.accept(this, level + 1);
                params = params.tail;
            }

            node.body.accept(this, level + 1);
            if (!currentFunctionReturnType.equals("void") && !hasReturn && !node.func_name.equals("main")) {
                errorFlag = true;
                errorOutput = errorOutput + "\n[ERROR] Function '" + node.func_name + "' must have a return statement at line " + (node.row + 1) + " and column " + (node.col + 1);
            }
            symbolTable.exitScope();
            //system.out.println("[EXIT] Scope for function '" + node.func_name + "'");
			//printer.printLevel("[EXIT] Scope for function '" + node.func_name + "'", level);
            currentFunctionReturnType = null;
            firstCompoundInFunction = false;
        }
    }

    @Override
    public void visit(SimpleDec node, int level) {
        //system.out.println("[VISIT] SimpleDec '" + node.name + "' at level " + level);
		//printer.printLevel("[VISIT] SimpleDec '" + node.name + "' at level " + level, level);
        boolean success = symbolTable.insert(node.name, node.type.type, 0, 0, 0);
        if (!success) {
            errorFlag = true;
            errorOutput = errorOutput + "\n[ERROR] Variable '" + node.name + "' is already declared in this scope " + " at line " + (node.row + 1) + " and column " + (node.col + 1);
        }
    }

    @Override
    public void visit(ArrayDec node, int level) {
        //system.out.println("[VISIT] ArrayDec '" + node.name + "' at level " + level);
		//printer.printLevel("[VISIT] ArrayDec '" + node.name + "' at level " + level, level);
        if (node.size == 0) {
            boolean success = symbolTable.insert(node.name, node.type.type, 1, 0, 0);
            if (!success) {
                errorFlag = true;
                errorOutput = errorOutput + "\n[ERROR] Array '" + node.name + "' is already declared in this scope" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            }
        }
        else {
            boolean success = symbolTable.insert(node.name, node.type.type, node.size, 0, 0);
            if (!success) {
                errorFlag = true;
                errorOutput = errorOutput + "\n[ERROR] Array '" + node.name + "' is already declared in this scope" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            }
        }
    }

    @Override
    public void visit(CompoundExp node, int level) {
        boolean enteredScope = false;
        // System.out.println("[ENTER] Compound Statement Scope at level " + level);
		//printer.printLevel("[ENTER] Compound Statement Scope at level " + level, level);
        if (firstCompoundInFunction) {
            firstCompoundInFunction = false;
        } else {
            enteredScope = true;
            symbolTable.enterScope();
        }

        if (node.decs != null) {
            node.decs.accept(this, level);
        }
        
        if (node.exps != null) {
            //system.out.println("[VISIT] Visiting expressions in CompoundExp at level " + level);
			//printer.printLevel("[VISIT] Visiting expressions in CompoundExp at level " + level, level);
            node.exps.accept(this, level); // Ensure expressions are visited!
        }
        if (!firstCompoundInFunction && enteredScope) {
            symbolTable.exitScope();
        }
        //system.out.println("[EXIT] Compound Statement Scope at level " + level);
		//printer.printLevel("[EXIT] Compound Statement Scope at level " + level, level);
    }    
    @Override
    public void visit(AssignExp node, int level) {
        String lhsType = null;
        int lhsDim = 0;
        String varName = null;
    
        if (node.lhs instanceof VarExp) {
            VarExp varExp = (VarExp) node.lhs;
    
            if (varExp.variable instanceof SimpleVar) {
                varName = ((SimpleVar) varExp.variable).name;
            } else if (varExp.variable instanceof IndexVar) {
                varName = ((IndexVar) varExp.variable).name;
                visit((IndexVar) varExp.variable, level);
                String indexType = getExpressionType(((IndexVar) varExp.variable).index);
                if (!indexType.equals("int")) {
                    errorOutput = errorOutput + "\n[ERROR] Array index must be of type int but is instead type " + indexType + " at line " + (node.row + 1) + " and column " + (node.col + 1);
                    errorFlag = true;
                    return;
                }
            }
    
            if (varName != null) {
                SymbolEntry lhsEntry = symbolTable.lookup(varName);
                if (lhsEntry == null) {
                    errorOutput = errorOutput + "\n[ERROR] Variable '" + varName + "' is undeclared at line " + (node.row + 1) + " and column " + (node.col + 1);
                    errorFlag = true;
                    return;
                } else {
                    lhsType = getTypeFromEntry(lhsEntry);
                    lhsDim = lhsEntry.dim;
                }
            }
        } else {
            errorOutput = errorOutput + "\n[ERROR] LHS of assignment is not a variable at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
            return;
        }
    
        String rhsType = getExpressionType(node.rhs);
    
        if (lhsType != null && rhsType != null && !lhsType.equals(rhsType)) {
            errorOutput = errorOutput + "\n[ERROR] Type mismatch in assignment: Cannot assign '" + rhsType + "' to '" + lhsType + "' at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
    
        // Check for invalid assignment of a scalar value to an array
        if (lhsDim > 0 && varName != null && node.lhs instanceof VarExp && ((VarExp) node.lhs).variable instanceof SimpleVar) {
            errorOutput = errorOutput + "\n[ERROR] Cannot assign a scalar value to an array '" + varName + "' at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
    
        node.rhs.accept(this, level);
    }
    
    
    

    @Override
    public void visit(OpExp node, int level) {
        String leftType = "unknown";
        String rightType = "unknown";
    
        int leftDim = 0;
        int rightDim = 0;
    
        boolean isUnaryMinus = (node.op == OpExp.UMINUS);
        
        // 🔹 Handle Binary Operations
        leftType = getExpressionType(node.left);
        rightType = getExpressionType(node.right);
    
        if (node.left instanceof VarExp) {
            VarExp varExp = (VarExp) node.left;
            if (varExp.variable instanceof SimpleVar) {
                SymbolEntry varEntry = symbolTable.lookup(((SimpleVar) varExp.variable).name);
                if (varEntry != null) leftDim = varEntry.dim;
            } else if (varExp.variable instanceof IndexVar) {
                leftDim = 0;  // Indexing an array results in a scalar
            }
        }
    
        if (node.right instanceof VarExp) {
            VarExp varExp = (VarExp) node.right;
            if (varExp.variable instanceof SimpleVar) {
                SymbolEntry varEntry = symbolTable.lookup(((SimpleVar) varExp.variable).name);
                if (varEntry != null) rightDim = varEntry.dim;
            } else if (varExp.variable instanceof IndexVar) {
                rightDim = 0;
            }
        }
    
        // 🔹 Prevent operations involving arrays
        if (leftDim > 0 || rightDim > 0) {
            errorOutput = errorOutput + "\n[ERROR] Cannot perform arithmetic operation on arrays at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
            return;
        }
        if (isUnaryMinus && node.left instanceof NilExp) {
            rightType = getExpressionType(node.right);
    
            if (!rightType.equals("int")) {
                errorOutput = errorOutput + "\n[ERROR] Unary '-' can only be applied to integers at line " + (node.row + 1) + " and column " + (node.col + 1);
                errorFlag = true;
            }
            return;
        }
        else if (!leftType.equals(rightType)) {
            errorOutput = errorOutput + "\n[ERROR] Type mismatch in operation: Cannot apply operator to '" + leftType + "' and '" + rightType + "' at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
    }
    
    

    @Override
    public void visit(CallExp node, int level) {
        SymbolEntry entry = symbolTable.lookup(node.func);
    
        if (entry == null) {
            errorOutput = errorOutput + "\n[ERROR] Function '" + node.func + "' is undefined at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
            return;
        }
    
        int expectedArgCount = entry.dim;  // `dim` stores the expected number of parameters
        int actualArgCount = 0;
        ExpList args = node.args;
    
        List<Integer> expectedTypes = entry.paramTypes;
        List<Integer> expectedDims = entry.paramDims;
        List<Integer> actualTypes = new ArrayList<>();
        List<Integer> actualDims = new ArrayList<>();
    
        while (args != null && args.head != null) {
            int argType = -1;
            int argDim = 0;
    
            if (args.head instanceof BoolExp) {
                argType = 0; // Bool type
            } else if (args.head instanceof IntExp) {
                argType = 1; // Int type
            } else if (args.head instanceof VarExp) {
                VarExp varExp = (VarExp) args.head;
                if (varExp.variable instanceof SimpleVar) {
                    String varName = ((SimpleVar) varExp.variable).name;
                    SymbolEntry varEntry = symbolTable.lookup(varName);
                    if (varEntry != null) {
                        argType = varEntry.type;
                        argDim = varEntry.dim; // Store actual dimension
                    } else {
                        errorOutput = errorOutput + "\n[ERROR] Variable '" + varName + "' is undeclared at line " + (node.row + 1) + " and column " + (node.col + 1);
                        errorFlag = true;
                    }
                } else if (varExp.variable instanceof IndexVar) {
                    String varName = ((IndexVar) varExp.variable).name;
                    SymbolEntry varEntry = symbolTable.lookup(varName);
                    if (varEntry != null) {
                        argType = varEntry.type;
                        argDim = 0;  // Indexing an array results in a scalar value
                    } else {
                        errorOutput = errorOutput + "\n[ERROR] Indexed variable '" + varName + "' is undeclared at line " + (node.row + 1) + " and column " + (node.col + 1);
                        errorFlag = true;
                    }
                }
            }
    
            actualTypes.add(argType);
            actualDims.add(argDim);
            args.head.accept(this, level);
            args = args.tail;
            actualArgCount++;
        }
    
        // 🔹 Check for argument count mismatch
        if (actualArgCount != expectedArgCount) {
            errorOutput = errorOutput + "\n[ERROR] Function '" + node.func + "' expects " + expectedArgCount + " arguments but got " + actualArgCount + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
            return;
        }
    
        // 🔹 Validate argument types
        for (int i = 0; i < expectedArgCount; i++) {
            boolean expectedIsArray = expectedDims.get(i) > 0; // True if expected parameter is an array
            boolean actualIsArray = actualDims.get(i) > 0;     // True if argument is an array
    
            // Ensure expected arrays receive arrays and scalars receive scalars
            if (expectedIsArray != actualIsArray) {
                errorOutput = errorOutput + "\n[ERROR] Argument " + (i + 1) + " of function '" + node.func + "' expects " + 
                              (expectedIsArray ? "an array" : "a scalar") + " but got " + 
                              (actualIsArray ? "an array" : "a scalar") + 
                              " at line " + (node.row + 1) + " and column " + (node.col + 1);
                errorFlag = true;
            } 
    
            // Ensure base types (`int`, `bool`) match, but ignore specific array sizes
            else if (!expectedTypes.get(i).equals(actualTypes.get(i))) {
                errorOutput = errorOutput + "\n[ERROR] Argument " + (i + 1) + " of function '" + node.func + "' expects type '" + expectedTypes.get(i) + "' but got '" + actualTypes.get(i) + "' at line " + (node.row + 1) + " and column " + (node.col + 1);
                errorFlag = true;
            }
        }
    }
    
    
    

    @Override
    public void visit(IfExp node, int level) {
        //system.out.println("[VISIT] IfExp");
		//printer.printLevel("[VISIT] IfExp", level);
    
        node.test.accept(this, level);
        String conditionType = getExpressionType(node.test);
    
        if (!conditionType.equals("bool")) {
            errorOutput = errorOutput + "\n[ERROR] Condition in if-statement must be  'bool', but got '" + conditionType + " at line " + (node.row + 1) + " and column " + (node.col + 1);
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
            errorOutput = errorOutput + "\n[ERROR] Condition in if-statement must be  'bool', but got '" + conditionType + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
        node.body.accept(this, level);
    }

    @Override
    public void visit(ReturnExp node, int level) {
        hasReturn = true;
        //system.out.println("[VISIT] ReturnExp");
		//printer.printLevel("[VISIT] ReturnExp", level);
    
        if (currentFunctionReturnType == null) {
            errorOutput = errorOutput + "\n[ERROR] Return statement found outside of a function" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
            return;
        }
    
        if (node.exp == null) {
            if (!currentFunctionReturnType.equals("void")) {
                errorOutput = errorOutput + "\n[ERROR] Missing return value in non-void function" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
                errorFlag = true;
            }
            return;
        }
    
        node.exp.accept(this, level);
        String returnType = getExpressionType(node.exp);
        if (!returnType.equals(currentFunctionReturnType) && !currentFunctionReturnType.equals("void")) {
            errorOutput = errorOutput + "\n[ERROR] Function must return '" + currentFunctionReturnType + "', but got '" + returnType + "' at line " + (node.row + 1) + " and column " + (node.col + 1);
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
                errorOutput = errorOutput + "\n[ERROR] Variable '" + varName + "' is undeclared" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
                errorFlag = true;
            } else {
                //system.out.println("[LOOKUP] Found variable '" + varName + "' in Scope: " + entry.scope);
				//printer.printLevel("[LOOKUP] Found variable '" + varName + "' in Scope: " + entry.scope, level);
            }
        } else if (node.variable instanceof IndexVar) {
            String varName = ((IndexVar) node.variable).name;
            SymbolEntry entry = symbolTable.lookup(varName);
    
            if (entry == null) {
                errorOutput = errorOutput + "\n[ERROR] Array '" + varName + "' is undeclared" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
                errorFlag = true;
            } else if (entry.dim <= 0) {
                errorOutput = errorOutput + "\n[ERROR] '" + varName + "' is not an array "+ " at line " + (node.row + 1) + " and column " + (node.col + 1);
                errorFlag = true;
            } else {
                //system.out.println("[LOOKUP] Found array '" + varName + "' in Scope: " + entry.scope);
				//printer.printLevel("[LOOKUP] Found array '" + varName + "' in Scope: " + entry.scope, level);
            }
        } else {
            errorOutput = errorOutput + "\n[ERROR] Unknown variable type in VarExp" + " at line " + (node.row + 1) + " and column " + (node.col + 1);
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
    @Override
    public void visit(IndexVar node, int level) {
        SymbolEntry entry = symbolTable.lookup(node.name);
    
        if (entry == null) {
            errorOutput = errorOutput + "\n[ERROR] Undeclared array variable '" + node.name + "' at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
            return;
        } else if (entry.dim <= 0) {
            errorOutput = errorOutput + "\n[ERROR] '" + node.name + "' is not an array at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
            return;
        }
    
        String indexType = getExpressionType(node.index);
        int indexDim = 0;

        if (node.index instanceof VarExp) {
            VarExp varExp = (VarExp) node.index;
            if (varExp.variable instanceof SimpleVar) {
                SymbolEntry indexEntry = symbolTable.lookup(((SimpleVar) varExp.variable).name);
                if (indexEntry != null) {
                    indexDim = indexEntry.dim;
                }
            } else if (varExp.variable instanceof IndexVar) {
                indexDim = 0;
            }
        }
    
        if (!indexType.equals("int")) {
            errorOutput = errorOutput + "\n[ERROR] Array index for '" + node.name + "' must be an integer at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
    
        if (indexDim > 0) {
            errorOutput = errorOutput + "\n[ERROR] Array index for '" + node.name + "' cannot be an array at line " + (node.row + 1) + " and column " + (node.col + 1);
            errorFlag = true;
        }
    
        node.index.accept(this, level);
    }
    

    private boolean isTypeCompatible(Exp left, Exp right) {
        String leftType = getExpressionType(left);
        String rightType = getExpressionType(right);
    
        System.out.println("[DEBUG] Comparing types: " + leftType + " vs " + rightType);
		//printer.printMsg("[DEBUG] Comparing types: " + leftType + " vs " + rightType);
    
        return leftType.equals(rightType);
    }
    

    private String getType(VarDec node) {
        if (node instanceof SimpleDec) return ((SimpleDec) node).type.toString();
        if (node instanceof ArrayDec) return ((ArrayDec) node).type.toString();
        return "unknown";
    }

    private List<String> getFunctionParameterTypes(String functionName) {
        List<String> paramTypes = new ArrayList<>();
        SymbolEntry entry = symbolTable.lookup(functionName);
    
        if (entry != null && entry.paramTypes != null) {
            for (int i = 0; i < entry.paramTypes.size(); i++) {
                String typeStr = getTypeAsString(entry.paramTypes.get(i));
                if (entry.paramDims.get(i) > 0) { // If it has a dimension, it's an array
                    typeStr += "[]";
                }
                paramTypes.add(typeStr);
            }
        }
        return paramTypes;
    }

    
    private String getTypeFromEntry(SymbolEntry entry) {
        if (entry.type == 0) return "bool";
        if (entry.type == 1) return "int";
        if (entry.type == 2) return "void";
        return "unknown";
    }

    private int getTypeFromString(String type) {
        switch (type) {
            case "bool":
                return 0;
            case "int":
                return 1;
            case "void":
                return 2;
            default:
                return -1;
        }
    }

    
    private String getTypeAsString(int type) {
        switch (type) {
            case 0:
                return "bool";
            case 1:
                return "int";
            case 2:
                return "void";
            default:
                return "null";
        }
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
            else if (varExp.variable instanceof IndexVar) {
                String varName = ((IndexVar) varExp.variable).name;
                SymbolEntry entry = symbolTable.lookup(varName);
                
                if (entry != null) {
                    if (entry.dim > 0) {
                        return getTypeFromEntry(entry);
                    } else {
                        errorOutput = errorOutput + "\n[ERROR] Variable '" + varName + "' is not an array but was indexed at line " + (expr.row + 1) + " and column " + (expr.col + 1);
                        errorFlag = true;
                        return "unknown";
                    }
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
				//printer.printMsg("[LOOKUP] Function '" + callExp.func + "' returns type '" + getTypeFromEntry(entry) + "'");
                return getTypeFromEntry(entry);
            } else {
                return "unknown";
                // errorOutput = errorOutput + "\n[ERROR] Function '" + callExp.func + "' is undefined" + " at line " + (node.row + 1) + " and column " + (node.col + 1));
            }
        }
        if (expr instanceof OpExp) {
            OpExp opExp = (OpExp) expr;
            String leftType = getExpressionType(opExp.left);
            String rightType = getExpressionType(opExp.right);
    
            if (opExp.op == OpExp.EQ || opExp.op == OpExp.LT || opExp.op == OpExp.GT) {
                return "bool";
            }
            boolean isUnaryMinus = (opExp.op == OpExp.UMINUS);
            if (isUnaryMinus && opExp.left instanceof NilExp) {
                rightType = getExpressionType(opExp.right);
        
                if (!rightType.equals("int")) {
                    errorOutput = errorOutput + "\n[ERROR] Unary '-' can only be applied to integers at line " + (opExp.row + 1) + " and column " + (opExp.col + 1);
                    errorFlag = true;
                }
                return "int";
            }

            boolean isUnaryNot = (opExp.op == OpExp.NOT);
            if (isUnaryNot && opExp.left instanceof NilExp) {
                rightType = getExpressionType(opExp.right);
        
                if (!rightType.equals("bool")) {
                    errorOutput = errorOutput + "\n[ERROR] Unary '~' can only be applied to bools at line " + (opExp.row + 1) + " and column " + (opExp.col + 1);
                    errorFlag = true;
                }
                return "bool";
            }
                
            if (!leftType.equals(rightType)) {
                // errorOutput = errorOutput + "\n[ERROR] Type mismatch in operation: Cannot apply operator to '" + leftType + "' and '" + rightType + " at line " + (node.row + 1) + " and column " + (node.col + 1));
                return "unknown";
            }
    
            return leftType; 
        }
        return "unknown";
    }
    
}