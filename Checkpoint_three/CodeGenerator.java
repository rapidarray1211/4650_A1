import absyn.*;
import java.io.*;
import java.util.*;

public class CodeGenerator implements AbsynVisitor {
    private TMWriter tm;
    private int labelCounter = 0;
    private int tempOffset = -1;
    private int currentLocalOffset = 0;
    private int mainEntry = -1;
    private int globalOffset = -1;

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
    }

    public void visit(Absyn root) throws IOException {
        System.out.println("[CG] Visiting Root Node");
        emitPrelude();
        root.accept(this, 0, false);
        emitFinale();
        tm.close();
    }

    private void emitPrelude() throws IOException {
        tm.emitComment("Standard prelude:");

        tm.emitRM("LD", GP, 0, AC, "load gp with maxaddress");
        tm.emitRM("LDA", FP, 0, GP, "copy gp to fp"); 
        tm.emitRM("ST", AC, 0, AC, "clear location 0");

        int savedLoc = tm.emitSkip(1);
        tm.emitComment("Jump around i/o routines here");

        tm.emitComment("code for input routine");
        tm.emitRM("ST", AC, -1, FP, "store return");  
        tm.emitRO("IN", AC, 0, 0, "input");
        tm.emitRM("LD", PC, -1, FP, "return to caller");

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
        tm.emitRM_Abs("LDA", PC, mainEntry + 1, "jump to main loc");
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(AssignExp node, int level, boolean isAddr) {
        System.out.println("[CG] AssignExp");
        try {
            tm.emitComment("-> op");
            node.rhs.accept(this, level, false);
            if (node.lhs != null && node.lhs.variable instanceof SimpleVar) {
                String name = ((SimpleVar) node.lhs.variable).name;
                Integer offset = localVarOffsets.get(name);
                if (offset != null) {
                    tm.emitRM("ST", AC, offset, GP, "Store to variable '" + name + "'");
                } else {
                    tm.emitComment("[ERROR] Undeclared variable: " + name);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void visit(OpExp node, int level, boolean isAddr) {
        System.out.println("[CG] OpExp");
        try {
            tm.emitComment("Binary Operation");
            node.left.accept(this, level, false);
            tm.emitRM("ST", AC, tempOffset, GP, "Push left operand to temp stack");
            node.right.accept(this, level, false);
            tm.emitRM("LD", AC1, tempOffset, GP, "Load left operand into R1");

            switch (node.op) {
                case OpExp.PLUS:    tm.emitRO("ADD", AC, AC1, AC, "R0 = R1 + R0"); break;
                case OpExp.MINUS:   tm.emitRO("SUB", AC, AC1, AC, "R0 = R1 - R0"); break;
                case OpExp.TIMES:   tm.emitRO("MUL", AC, AC1, AC, "R0 = R1 * R0"); break;
                case OpExp.OVER:
                case OpExp.DIVIDE:  tm.emitRO("DIV", AC, AC1, AC, "R0 = R1 / R0"); break;
                case OpExp.EQ:      tm.emitRO("TEQ", AC, AC1, AC, "R0 = R1 == R0"); break;
                case OpExp.NEQ:     tm.emitRO("TNE", AC, AC1, AC, "R0 = R1 != R0"); break;
                case OpExp.LT:      tm.emitRO("TLT", AC, AC1, AC, "R0 = R1 < R0"); break;
                case OpExp.GT:      tm.emitRO("TGT", AC, AC1, AC, "R0 = R1 > R0"); break;
                case OpExp.LTE:     tm.emitRO("TLE", AC, AC1, AC, "R0 = R1 <= R0"); break;
                case OpExp.GTE:     tm.emitRO("TGE", AC, AC1, AC, "R0 = R1 >= R0"); break;
                default:
                    tm.emitComment("Unsupported operator: " + node.op);
            }
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
            tm.emitComment("Function " + node.func_name);
			
			node.funaddr = tm.getCurrentLoc();
			
            if ("main".equals(node.func_name)) {
                mainEntry = tm.getCurrentLoc();
            }
            localVarOffsets.clear();
            currentLocalOffset = -1;
            if (node.body instanceof CompoundExp) {
                node.body.accept(this, level + 1, isAddr);
            } else {
                tm.emitComment("[WARN] Function body is not a CompoundExp");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(SimpleDec node, int level, boolean isAddr) {
        System.out.println("[CG] SimpleDec: " + node.name);
        try {
            currentLocalOffset--;
            localVarOffsets.put(node.name, currentLocalOffset);
            tm.emitComment("Variable Declaration: " + node.name + " at offset " + currentLocalOffset);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public void visit(CompoundExp node, int level, boolean isAddr) {
        System.out.println("[CG] CompoundExp");
        if (node.decs != null) node.decs.accept(this, level + 1, isAddr);
        if (node.exps != null) node.exps.accept(this, level + 1, isAddr);
    }

    @Override
    public void visit(VarExp node, int level, boolean isAddr) {
        System.out.println("[CG] VarExp");
        try {
            if (node.variable instanceof SimpleVar) {
                String name = ((SimpleVar) node.variable).name;
                Integer offset = localVarOffsets.get(name);
                if (offset == null) {
                    tm.emitComment("[ERROR] Undeclared variable: " + name);
                    return;
                }
                if (isAddr) {
                    tm.emitRM("LDA", AC, offset, GP, "Get address of variable '" + name + "'");
                } else {
                    tm.emitRM("LD", AC, offset, GP, "Load value of variable '" + name + "'");
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
            int jumpToEndLoc = -1;
            if (exp.elsepart != null) {
                tm.emitComment("if: jump to end belongs here");
                jumpToEndLoc = tm.emitSkip(1);
            }
            int currLoc = tm.emitSkip(0);
            tm.emitBackup(jumpToElseLoc);
            tm.emitRM("JEQ", AC, currLoc - jumpToElseLoc, PC, "if: jmp to else");
            tm.emitRestore();
            if (exp.elsepart != null) {
                exp.elsepart.accept(this, offset, isAddr);
                currLoc = tm.emitSkip(0);
                tm.emitBackup(jumpToEndLoc);
                tm.emitRM("LDA", PC, currLoc - jumpToEndLoc, PC, "if: jmp to end");
                tm.emitRestore();
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
			currentLocalOffset -= exp.size;
			localVarOffsets.put(exp.name, currentLocalOffset);
			tm.emitComment("Array Declaration: " + exp.name + " with size " + exp.size + " at offset " + currentLocalOffset);
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
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void visit(IndexVar exp, int level, boolean isAddr) {
		System.out.println("[CG] IndexVar: " + exp.name);
		try {
			exp.index.accept(this, level, false);
			Integer baseOffset = localVarOffsets.get(exp.name);
			if (baseOffset == null) {
				tm.emitComment("[ERROR] Undeclared array: " + exp.name);
				return;
			}
			tm.emitRM("LDA", AC1, baseOffset, GP, "Load base address of array '" + exp.name + "'");
			tm.emitRO("ADD", AC, AC1, AC, "Compute indexed address");
			if (!isAddr) {
				tm.emitRM("LD", AC, 0, AC, "Load array element value");
			}
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
	public void visit(SimpleVar exp, int level, boolean isAddr) {
		System.out.println("[CG] SimpleVar: " + exp.name);
		try {
			Integer offset = localVarOffsets.get(exp.name);
			if (offset == null) {
				tm.emitComment("[ERROR] Undeclared variable: " + exp.name);
				return;
			}
			if (isAddr) {
				tm.emitRM("LDA", AC, offset, GP, "Get address of variable '" + exp.name + "'");
			} else {
				tm.emitRM("LD", AC, offset, GP, "Load value of variable '" + exp.name + "'");
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
			
			int loopStart = tm.getCurrentLoc();
			
			exp.test.accept(this, level, false);
			
			int jumpToEndLoc = tm.emitSkip(1);
			
			exp.body.accept(this, level, isAddr);
			
			tm.emitRM("LDA", PC, loopStart - tm.getCurrentLoc(), PC, "while: jump back to condition");
			
			int currentLoc = tm.getCurrentLoc();
			tm.emitBackup(jumpToEndLoc);
			tm.emitRM("JEQ", AC, currentLoc - jumpToEndLoc, PC, "while: exit loop if condition is false");
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
			if(exp.args != null) {
				exp.args.accept(this, level, false);
			}
			tm.emitRM("LDA", AC, 1, PC, "Load return address");
			tm.emitRM("ST", AC, -1, FP, "Store return address");
			
			// Get the function address from the funcDef
			if (exp.funcDef != null) {
				int funcAddr = exp.funcDef.funaddr;
				tm.emitRM_Abs("LDA", PC, funcAddr, "Jump to function " + exp.func);
			} else {
				tm.emitComment("[ERROR] Function definition not found for: " + exp.func);
			}
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
