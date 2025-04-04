import Symbol.AnalyzerPrinter;
import Symbol.SymbolEntry;
import Symbol.SymbolTable;
import absyn.*;
import java.io.*;
import java.util.*;

public class CodeGenerator implements AbsynVisitor {
    private TMWriter tm;
    private int labelCounter = 0;
    private int tempOffset = -1;
    private int currentLocalOffset = -1;
    private int mainEntry = -1;
    private int globalOffset = 0;

    private SymbolTable symbolTable;
    private AnalyzerPrinter printer;

    public static final int AC = 0;
    public static final int AC1 = 1;
    public static final int FP = 5;
    public static final int GP = 6;
    public static final int PC = 7;

    private Map<String, Integer> localVarOffsets = new HashMap<>();

    public CodeGenerator(String outputFile) {
        try {
            this.tm = new TMWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            System.err.println("Failed to open output file: " + e.getMessage());
        }
        this.printer = new AnalyzerPrinter("symbTree.txt", false);
        symbolTable = new SymbolTable(printer);
    }

    public void visit(Absyn root) throws IOException {
        System.out.println("[CG] Visiting Root Node");
        symbolTable.enterScope("Global");
        emitPrelude();
        root.accept(this, 0, false);
        emitFinale();
        symbolTable.exitScope("Leaving Global");
        symbolTable.printTable();
        tm.close();
    }

    private void emitPrelude() throws IOException {
        tm.emitComment("Standard prelude:");

        tm.emitRM("LD", GP, 0, AC, "load gp with maxaddress");
        tm.emitRM("LDA", FP, 0, GP, "copy gp to fp"); 
        tm.emitRM("ST", AC, 0, AC, "clear location 0");

        int savedLoc = tm.emitSkip(1);
        tm.emitComment("Jump around i/o routines here");
        
        //add input to symbol table.
        symbolTable.insert("input",NameTy.INT,0,0,tm.getCurrentLoc());

        tm.emitComment("code for input routine");
        tm.emitRM("ST", AC, -1, FP, "store return");  
        tm.emitRO("IN", AC, 0, 0, "input");
        tm.emitRM("LD", PC, -1, FP, "return to caller");

        //add output to symbol table.
        symbolTable.insert("output",NameTy.VOID,1,0,tm.getCurrentLoc());

        tm.emitComment("code for output routine");
        tm.emitRM("ST", AC, -1, FP, "store return"); 
        tm.emitRM("LD", AC, -2, FP, "load output value");
        tm.emitRO("OUT", AC, 0, 0, "output");
        tm.emitRM("LD", PC, -1, FP, "return to caller");

        int afterIO = tm.getCurrentLoc();
        tm.emitBackup(savedLoc);
        tm.emitRM_Abs("LDA", PC, afterIO, "jump around i/o code");
        tm.emitRestore();

        tm.emitComment("End of standard prelude.");
    }

    private void emitFinale() throws IOException {
        tm.emitRM("ST", FP, 0, FP, "push ofp");
        tm.emitRM("LDA", FP, 0, FP, "push frame");
        tm.emitRM("LDA", AC, 1, PC, "load ac with ret ptr");
        tm.emitRM_Abs("LDA", PC, mainEntry, "jump to main loc");
        tm.emitRM("LD", FP, 0, FP, "pop frame");
        tm.emitRO("HALT", 0, 0, 0, "");
    }

    private int newLabel() {
        return labelCounter++;
    }

    @Override
    public void visit(IntExp node, int level, boolean isAddr) {
        System.out.println("[CG] IntExp: " + node.value);
        try {
            int value = Integer.parseInt(node.value);
            tm.emitComment("Integer literal: " + value);
            
            tm.emitRM("LDC", AC, value, 0, "Load constant into R0");

            if (isAddr == false){
                //Store to temp storage if on the right
                tm.emitRM("ST",AC,currentLocalOffset,FP,"Store Constant to temp");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void visit(AssignExp node, int level, boolean isAddr) {
        System.out.println("[CG] AssignExp");
        try {
            tm.emitComment("-> Assign");
        
            //VarExp should store address in temp storage
            node.lhs.accept(this,level,true);

            //Right expression value should be in AC, increment local offset so we don't
            //overwrite temp storage of VarExp address
            currentLocalOffset --;
            node.rhs.accept(this, level, true);
            currentLocalOffset ++;
    
            tm.emitRM("LD", AC1, currentLocalOffset, FP, "Load target address");
            tm.emitRM("ST", AC, 0, AC1, "Store value to address");
            tm.emitComment("<- Assign");
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    


    @Override
    public void visit(OpExp node, int level, boolean isAddr) {
        System.out.println("[CG] OpExp");
        try {
            //Use isAddr  to determine if the expression is left or right. If the expression is left then the result
            //is stored in AC, if expression is right the result is stored in Temp Location in stack.
            //Also perform Right expression first so it doesn't overwrite AC.

            tm.emitComment("Binary Operation");
            node.right.accept(this, level, false);

            //increment local offset to make sure previous local offset isn't overwritten
            currentLocalOffset --;
            node.left.accept(this, level, true);
            currentLocalOffset ++;

            //Load value of right expression
            tm.emitRM("LD", AC1, currentLocalOffset, FP, "Load right expression into R1");

            switch (node.op) {
                case OpExp.PLUS:    tm.emitRO("ADD", AC, AC, AC1, "R0 = R0 + R1"); break;
                case OpExp.MINUS:   tm.emitRO("SUB", AC, AC, AC1, "R0 = R0 - R1"); break;
                case OpExp.TIMES:   tm.emitRO("MUL", AC, AC, AC1, "R0 = R0 * R1"); break;
                case OpExp.DIVIDE:  tm.emitRO("DIV", AC, AC, AC1, "R0 = R0 / R1"); break;
                case OpExp.EQ: {
                    tm.emitRO("SUB", AC, AC, AC1, "op ==");
                    tm.emitRM("JEQ", AC, 2,  PC, "branch if equal");
                    tm.emitRM("LDC", AC, 0, 0, "false case");
                    tm.emitRM("LDA", PC, 1, PC, "unconditional jump");
                    tm.emitRM("LDC", AC, 1, 0, "true case");
                    break;
                }                
                case OpExp.NEQ: {
                    tm.emitRO("SUB", AC, AC, AC1, "op !=");
                    tm.emitRM("JNE", AC, 2,  PC, "branch if not equal");
                    tm.emitRM("LDC", AC, 0, 0, "false case");
                    tm.emitRM("LDA", PC, 1, PC, "unconditional jump");
                    tm.emitRM("LDC", AC, 1, 0, "true case");
                    break;
                }                
                case OpExp.LT: {
                    tm.emitRO("SUB", AC, AC, AC1, "op <");
                    tm.emitRM("JLT", AC, 2,  PC, "branch if lt");
                    tm.emitRM("LDC", AC, 0, 0, "false case");
                    tm.emitRM("LDA", PC, 1, PC, "unconditional jump");
                    tm.emitRM("LDC", AC, 1, 0, "true case");
                    break;
                }                
                case OpExp.GT: {
                    tm.emitRO("SUB", AC, AC, AC1, "op >");
                    tm.emitRM("JGT", AC, 2,  PC, "branch if gt");
                    tm.emitRM("LDC", AC, 0, 0, "false case");
                    tm.emitRM("LDA", PC, 1, PC, "unconditional jump");
                    tm.emitRM("LDC", AC, 1, 0, "true case");
                    break;
                }                                                    
                case OpExp.LTE: {
                    tm.emitRO("SUB", AC, AC, AC1, "op <=");
                    tm.emitRM("JLE", AC, 2,  PC, "branch if lte");
                    tm.emitRM("LDC", AC, 0, 0, "false case");
                    tm.emitRM("LDA", PC, 1, PC, "unconditional jump");
                    tm.emitRM("LDC", AC, 1, 0, "true case");
                    break;
                }                
                case OpExp.GTE: {
                    tm.emitRO("SUB", AC, AC, AC1, "op >=");
                    tm.emitRM("JGE", AC, 2,  PC, "branch if gte");
                    tm.emitRM("LDC", AC, 0, 0, "false case");
                    tm.emitRM("LDA", PC, 1, PC, "unconditional jump");
                    tm.emitRM("LDC", AC, 1, 0, "true case");
                    break;
                } 
                case OpExp.AND: break;
                case OpExp.OR: break;
                case OpExp.NOT: break;
                case OpExp.UMINUS: break;
                default:
                    tm.emitComment("Unsupported operator: " + node.op);
            }
              
            //False indicates that this is on the right so store AC into temp.
            if (isAddr == false){
                tm.emitRM("ST",AC,currentLocalOffset,FP,"Store AC to temp");
            }
            
            tm.emitComment("<- op");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override public void visit(DecList node, int level, boolean isAddr) {
        System.out.println("[CG] DecList");
        while (node != null) {
            if (node.head != null) node.head.accept(this, level, isAddr);
            node = node.tail;
        }
    }

    @Override
    public void visit(FunctionDec node, int level, boolean isAddr) {
        System.out.println("[CG] FunctionDec: " + node.func_name);
        try {
            tm.emitComment("Jump around function body here");
            int savedLoc = tm.emitSkip(1);
            tm.emitComment("Function " + node.func_name);
			
			node.funaddr = tm.getCurrentLoc();
			
            if ("main".equals(node.func_name)) {
                mainEntry = tm.getCurrentLoc();
            }

            if (node.body instanceof CompoundExp) {
                SymbolEntry protFunc = symbolTable.lookup(node.func_name);

                if (protFunc != null){
                    //If prototype is found, make a jump to the function
                    int currLoc = tm.getCurrentLoc();
                    int protLoc = protFunc.pc;
                    tm.emitBackup(protLoc);
                    tm.emitRM("LDA",PC,currLoc - protLoc,PC,"Jump to function from prototype");
                    tm.emitRestore();
                    
                }
                symbolTable.insert(node.func_name,node.return_type.type,0,0,tm.getCurrentLoc());
                symbolTable.enterScope(node.func_name);
                currentLocalOffset = -1;
                tm.emitRM("ST", AC,currentLocalOffset, FP, "Store return value");
                currentLocalOffset --;
                node.parameters.accept(this, level + 1, isAddr);
                node.body.accept(this, level + 1, isAddr);
                symbolTable.exitScope(node.func_name);
            } else {
                //If prototype, skip loc for backpatch
                tm.emitComment("Prototype function, Jump to function here.");
                symbolTable.insert(node.func_name, node.return_type.type, 0, 0, tm.emitSkip(1));

            }
            int currLoc = tm.getCurrentLoc();
            tm.emitBackup(savedLoc);
            tm.emitRM("LDA",PC,currLoc-savedLoc,PC,"Jump around function");
            tm.emitRestore();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    

    @Override
    public void visit(SimpleDec node, int level, boolean isAddr) {
        System.out.println("[CG] SimpleDec: " + node.name);
        try {
            if (symbolTable.getCurrentScope() > 0){
                System.out.println("Inserting simpledec into symbol table at offset " + currentLocalOffset + " at node.name:" + node.name );
                symbolTable.insert(node.name, node.type.type,0, currentLocalOffset, 0);            
                tm.emitComment("Variable Declaration: " + node.name + " at local offset " + currentLocalOffset);
                currentLocalOffset --;
            } else{
                System.out.println("Inserting simpledec into symbol table at offset " + globalOffset + " at node.name:" + node.name );
                symbolTable.insert(node.name, node.type.type,0, globalOffset, 0);            
                tm.emitComment("Variable Declaration: " + node.name + " at global offset " + globalOffset);
                globalOffset --;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void visit(CompoundExp node, int level, boolean isAddr) {
        System.out.println("[CG] CompoundExp");
        try {
            tm.emitComment("-> compound statement");
            if (node.decs != null) node.decs.accept(this, level + 1, isAddr);
            if (node.exps != null) node.exps.accept(this, level + 1, isAddr);
            tm.emitComment("<- compound statement");
        }
        catch (Exception e){}
    }

    @Override
    //Done but needs indexvar
    public void visit(VarExp node, int level, boolean isAddr) {
        System.out.println("[CG] VarExp");
        try {
            if (node.variable instanceof SimpleVar variable) {
                String name = variable.name;
                
                SymbolEntry entry = symbolTable.lookup(name);
                System.out.println("[DEBUG] Lookup '" + name + "' => offset=" + entry.offset + " scope=" + entry.scope);

                int baseReg = entry.scope == 0 ? GP : FP;

                //Store the address of variable in temp if true and also keep value in AC.
                //If False, store value in temp.                 
                if (isAddr) {
                    tm.emitRM("LDA", AC, entry.offset, baseReg, "Get address of variable '" + name + "'");
                    tm.emitRM("ST",AC,currentLocalOffset,FP,"Store Address");
                    tm.emitRM("LD", AC, entry.offset, baseReg, "Load value of variable '" + name + "'");
                } else {
                    tm.emitRM("LD", AC, entry.offset, baseReg, "Load value of variable '" + name + "'");
                    tm.emitRM("ST",AC,currentLocalOffset,FP,"Store Value");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    @Override public void visit(VarDecList node, int level, boolean isAddr) {
        System.out.println("[CG] VarDecList");
        while (node != null) {
            if (node.head != null) node.head.accept(this, level, isAddr);
            // currentLocalOffset--;
            node = node.tail;
        }
    }

    @Override public void visit(ExpList node, int level, boolean isAddr) {
        System.out.println("[CG] ExpList");
        while (node != null) {
            if (node.head != null) node.head.accept(this, level, isAddr);
            node = node.tail;
        }
    }

    @Override public void visit(WriteExp node, int level, boolean isAddr) {
        System.out.println("[CG] WriteExp");
        try {
            tm.emitComment("Write Expression");
            node.output.accept(this, level, false);
            tm.emitRO("OUT", AC, 0, 0, "Output R0");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(IfExp exp, int offset, boolean isAddr) {
        try {
            tm.emitComment("-> if");
            exp.test.accept(this, offset, false);
            tm.emitComment("if: jump to else belongs here");
            int jumpToElseLoc = tm.emitSkip(1);
            if (exp.thenpart != null) exp.thenpart.accept(this, offset, isAddr);
            int jumpToEndLoc = tm.emitSkip(0);
            if (exp.elsepart != null) {
                tm.emitComment("if: jump to end belongs here");
                jumpToEndLoc = tm.emitSkip(0);
            }
            tm.emitBackup(jumpToElseLoc);
            tm.emitRM_Abs("JEQ", 0, jumpToEndLoc, "if: jmp to else");
            tm.emitRestore();
            if (exp.elsepart != null) {
                exp.elsepart.accept(this, offset, isAddr);
            }
            tm.emitComment("<- if");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

	@Override
	public void visit(ReadExp exp, int level, boolean isAddr) {
		System.out.println("[CG] ReadExp");
		try {
			tm.emitComment("Read Expression");
			tm.emitRO("IN", AC, 0, 0, "Input value");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*@Override
	public void visit(RepeatExp exp, int level, boolean isAddr) {
		System.out.println("[CG] RepeatExp");
		try {
			tm.emitComment("-> repeat");
			
			// 1. Mark the start of the loop body
			int loopStart = tm.getCurrentLoc();
			
			// 2. Execute the body of the loop
			exp.exps.accept(this, level, isAddr);
			
			// 3. Evaluate the condition after executing the body
			exp.condition.accept(this, level, false);
			
			// 4. If condition is false (0), jump back to start of loop
			// NOTE: In RepeatExp, we typically loop while condition is false,
			// so we need to invert the logic compared to WhileExp
			tm.emitRM("JEQ", AC, loopStart - tm.getCurrentLoc(), PC, "repeat: jump back to start if condition is false");
			
			tm.emitComment("<- repeat");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/
	
	@Override
	public void visit(ArrayDec exp, int level, boolean isAddr) {
		System.out.println("[CG] ArrayDec: " + exp.name);
		try {
            if (symbolTable.getCurrentScope() > 0){
                System.out.println("Inserting into symbol table at offset " + currentLocalOffset + " at exp.name:" + exp.name );
                symbolTable.insert(exp.name, exp.type.type, exp.size + 1, currentLocalOffset, 0);
			    tm.emitComment("Array Declaration: " + exp.name + " with size " + exp.size + " at local offset " + currentLocalOffset);
                currentLocalOffset = currentLocalOffset - exp.size-1;
            }
            else{
                System.out.println("Inserting into symbol table at offset " + globalOffset + " at exp.name:" + exp.name );
                symbolTable.insert(exp.name, exp.type.type, exp.size + 1, globalOffset, 0);
			    tm.emitComment("Array Declaration: " + exp.name + " with size " + exp.size + " at global offset " + globalOffset);
                globalOffset = globalOffset - exp.size - 1;
            }
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void visit(BoolExp exp, int level, boolean isAddr) {
		System.out.println("[CG] BoolExp: " + exp.value);
		try {
			int val = exp.value ? 1 : 0;
			tm.emitComment("Boolean literal: " + val);
			tm.emitRM("LDC", AC, val, 0, "Load boolean constant into R0");

            if (!isAddr){
                //Store to temp sotrage if on right
                tm.emitRM("ST", AC, currentLocalOffset,FP,"Store to temp");
            }

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    @Override
    public void visit(IndexVar exp, int level, boolean isAddr) {
        System.out.println("[CG] IndexVar: " + exp.name);
        try {
            String name = exp.name;

            SymbolEntry entry = symbolTable.lookup(name);
            System.out.println("[DEBUG] Lookup '" + name + "' => offset=" + entry.offset + " scope=" + entry.scope);
            tm.emitComment("-> lookup Array:" + name );

            int baseReg = entry.scope == 0 ? GP : FP;

            tm.emitRM("LDA", AC, entry.offset, baseReg, "Get address of variable '" + name + "'");
            tm.emitRM("ST",AC,currentLocalOffset,FP,"Store Address");

            //Get the value of index, should be stored in AC
            exp.index.accept(this, level, true);

            tm.emitRM("JLT",AC,1,PC,"HALT if index < 0" );
            tm.emitRM("LDA",PC,1,PC, "Absolute jump if not");
            tm.emitRO("HALT",0,0,0,"Halt if index < 0");
            tm.emitRM("LD", AC1,currentLocalOffset, FP, "Load array base address");
            tm.emitRO("SUB", AC, AC1, AC, "address is stored in AC");

            if (isAddr) {
                //Additionaly store location of memory in temp
                tm.emitRM("ST",AC,currentLocalOffset,FP, "Store address in temp");

                tm.emitRM("LD", AC,0,AC, "Load value at array index");

            } else {

                tm.emitRM("LD", AC,0,AC, "Load value at array index");

                //Additionaly store value in temp
                tm.emitRM("ST",AC,currentLocalOffset,FP, "Store value in temp");
            }
            tm.emitComment("<- lookup Array:" + name );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
	
	@Override
	public void visit(NameTy exp, int level, boolean isAddr) {
		System.out.println("[CG] NameTy: " + exp.getTypeName(exp.type));
		try {
			tm.emitComment("Type: " + exp.getTypeName(exp.type));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
    @Override
    //done
    public void visit(SimpleVar exp, int level, boolean isAddr) {
        System.out.println("[CG] SimpleVar: " + exp.name);
        try {
            tm.emitComment("-> id");
            SymbolEntry entry = symbolTable.lookup(exp.name);
            if (entry == null) {
                tm.emitComment("[ERROR] Undeclared variable: " + exp.name);
                return;
            }
            int baseReg = entry.scope == 0 ? GP : FP;
            if (isAddr) {
                tm.emitRM("LDA", AC, entry.offset, baseReg, "Get address of variable '" + exp.name + "'");
            } else {
                tm.emitRM("LD", AC, entry.offset, baseReg, "Load value of variable '" + exp.name + "'");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
	
    @Override
    public void visit(WhileExp exp, int level, boolean isAddr) {
        System.out.println("[CG] WhileExp");
        try {
            tm.emitComment("-> while");
            tm.emitComment("while: evaluate condition");
            int savedLoc = tm.emitSkip(0);

            exp.test.accept(this, level, false);
            tm.emitComment("while: jump to end belongs here");

            int savedLoc2 = tm.emitSkip(1);
            tm.emitComment("while: begin loop body");
            exp.body.accept(this, level, false);
            tm.emitRM_Abs("LDA", PC, savedLoc, "while: jump back to test");

            int savedLoc3 = tm.getCurrentLoc();
            tm.emitBackup(savedLoc2);
            tm.emitRM_Abs("JEQ", 0, savedLoc3, "while: exit loop if false");
            tm.emitRestore();
            tm.emitComment("<- while");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
	@Override
	public void visit(NilExp exp, int level, boolean isAddr) {
		System.out.println("[CG] NilExp");
		try {
			tm.emitComment("Nil Expression: no operation");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void visit(ReturnExp exp, int level, boolean isAddr) {
		System.out.println("[CG] ReturnExp");
		try {
			if(exp.exp != null) {
				exp.exp.accept(this, level, false);
			}
			tm.emitRM("LD", PC, -1, FP, "Return from function");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
    
    @Override
    public void visit(CallExp exp, int level, boolean isAddr) {
        System.out.println("[CG] CallExp: " + exp.func);
        try {
            tm.emitComment("Call function: " + exp.func);
    
            int argOffset = -2;
            ExpList args = exp.args;
            while (args != null) {
                if (args.head != null) {
                    args.head.accept(this, level, false);
                    tm.emitRM("ST", AC, currentLocalOffset + argOffset, FP, "Store arg at offset " + argOffset);
                    argOffset--;
                }
                args = args.tail;
            }
            tm.emitRM("ST", FP, currentLocalOffset, FP, "push ofp");
            tm.emitRM("LDA", FP, currentLocalOffset, FP, "Push frame");
            tm.emitRM("LDA", 0, 1, PC, "Load ac with ret ptr");
            SymbolEntry f = symbolTable.lookup(exp.func);
            if (f != null) {
                tm.emitRM_Abs("LDA", PC, f.pc, "jump to fun loc");
            } else {
                tm.emitComment("[ERROR] Function " + exp.func + " not found in symbol table");
            }
            tm.emitRM("LD", FP, 0, FP, "pop frame");
            tm.emitComment("<- call");
    
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    

    









    //@Override public void visit(ReadExp exp, int level, boolean isAddr) {}
	@Override public void visit(RepeatExp exp, int level, boolean isAddr) {}
    //@Override public void visit(ArrayDec exp, int level, boolean isAddr) {}
    //@Override public void visit(BoolExp exp, int level, boolean isAddr) {}
    //@Override public void visit(IndexVar exp, int level, boolean isAddr) {}
    //@Override public void visit(NameTy exp, int level, boolean isAddr) {}
    //@Override public void visit(SimpleVar exp, int level, boolean isAddr) {}
    //@Override public void visit(WhileExp exp, int level, boolean isAddr) {}
    //@Override public void visit(NilExp exp, int level, boolean isAddr) {}
    //@Override public void visit(ReturnExp exp, int level, boolean isAddr) {}
    //@Override public void visit(CallExp exp, int level, boolean isAddr) {}
}
