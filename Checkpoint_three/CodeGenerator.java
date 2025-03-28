import absyn.*;
import java.io.*;
import java.util.*;

public class CodeGenerator implements AbsynVisitor {
    private TMWriter tm;
    private int labelCounter = 0;
    private int tempOffset = -1;
    private int currentLocalOffset = 0;

    // Local variable name → stack offset
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
        tm.emitComment("Program start");
        tm.emitRM("LD", 6, 0, 0, "Load frame pointer");
        tm.emitRM("LDA", 7, 1, 7, "Jump to main (placeholder)");
    }

    private void emitFinale() throws IOException {
        tm.emitComment("Program end");
        tm.emitRO("HALT", 0, 0, 0, "End execution");
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
            tm.emitRM("LDC", 0, value, 0, "Load constant into R0");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(AssignExp node, int level, boolean isAddr) {
        System.out.println("[CG] AssignExp");
        try {
            tm.emitComment("Assignment");
            node.rhs.accept(this, level, false);
            if (node.lhs != null && node.lhs.variable instanceof SimpleVar) {
                String name = ((SimpleVar) node.lhs.variable).name;
                Integer offset = localVarOffsets.get(name);
                if (offset != null) {
                    tm.emitRM("ST", 0, offset, 6, "Store to variable '" + name + "'");
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
            tm.emitRM("ST", 0, tempOffset, 6, "Push left operand to temp stack");
            node.right.accept(this, level, false);
            tm.emitRM("LD", 1, tempOffset, 6, "Load left operand into R1");

            switch (node.op) {
                case OpExp.PLUS:    tm.emitRO("ADD", 0, 1, 0, "R0 = R1 + R0"); break;
                case OpExp.MINUS:   tm.emitRO("SUB", 0, 1, 0, "R0 = R1 - R0"); break;
                case OpExp.TIMES:   tm.emitRO("MUL", 0, 1, 0, "R0 = R1 * R0"); break;
                case OpExp.OVER:
                case OpExp.DIVIDE:  tm.emitRO("DIV", 0, 1, 0, "R0 = R1 / R0"); break;
                case OpExp.EQ:      tm.emitRO("TEQ", 0, 1, 0, "R0 = R1 == R0"); break;
                case OpExp.NEQ:     tm.emitRO("TNE", 0, 1, 0, "R0 = R1 != R0"); break;
                case OpExp.LT:      tm.emitRO("TLT", 0, 1, 0, "R0 = R1 < R0"); break;
                case OpExp.GT:      tm.emitRO("TGT", 0, 1, 0, "R0 = R1 > R0"); break;
                case OpExp.LTE:     tm.emitRO("TLE", 0, 1, 0, "R0 = R1 <= R0"); break;
                case OpExp.GTE:     tm.emitRO("TGE", 0, 1, 0, "R0 = R1 >= R0"); break;
                default:
                    tm.emitComment("Unsupported operator: " + node.op);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(DecList node, int level, boolean isAddr) {
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
            // reset local vars for each function
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

    @Override
    public void visit(CompoundExp node, int level, boolean isAddr) {
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
                    tm.emitRM("LDA", 0, offset, 6, "Get address of variable '" + name + "'");
                } else {
                    tm.emitRM("LD", 0, offset, 6, "Load value of variable '" + name + "'");
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

    // Stub methods
    @Override public void visit(IfExp exp, int level, boolean isAddr) {}
    @Override public void visit(ReadExp exp, int level, boolean isAddr) {}
    @Override public void visit(RepeatExp exp, int level, boolean isAddr) {}
    @Override public void visit(WriteExp exp, int level, boolean isAddr) {}
    @Override public void visit(ArrayDec exp, int level, boolean isAddr) {}
    @Override public void visit(BoolExp exp, int level, boolean isAddr) {}
    @Override public void visit(IndexVar exp, int level, boolean isAddr) {}
    @Override public void visit(NameTy exp, int level, boolean isAddr) {}
    @Override public void visit(SimpleVar exp, int level, boolean isAddr) {}
    @Override public void visit(WhileExp exp, int level, boolean isAddr) {}
    @Override public void visit(NilExp exp, int level, boolean isAddr) {}
    @Override public void visit(CallExp exp, int level, boolean isAddr) {}
    @Override public void visit(ReturnExp exp, int level, boolean isAddr) {}
}
