import absyn.*;
import Symbol.*;
import java.io.*;

public class CodeGenerator implements AbsynVisitor {
    private TMWriter tm;
    private SymbolTable symtab;
    private int labelCounter = 0;
    private int tempOffset = -1;

    public CodeGenerator(SymbolTable symtab, String outputFile) {
        this.symtab = symtab;
        try {
            this.tm = new TMWriter(new FileWriter(outputFile));
        } catch (IOException e) {
            System.err.println("Failed to open output file: " + e.getMessage());
        }
    }

    public void generate(DecList root) throws IOException {
        emitPrelude();
        root.accept(this, 0);
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
    public void visit(IntExp node, int level) {
        try {
            int value = Integer.parseInt(node.value);
            tm.emitComment("Integer literal: " + value);
            tm.emitRM("LDC", 0, value, 0, "Load constant into R0");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(AssignExp node, int level) {
        try {
            tm.emitComment("Assignment");
            node.rhs.accept(this, level);
            if (node.lhs != null && node.lhs.variable instanceof SimpleVar) {
                String name = ((SimpleVar) node.lhs.variable).name;
                SymbolEntry entry = symtab.lookup(name);
                if (entry != null) {
                    tm.emitRM("ST", 0, entry.offset, 6, "Store to variable '" + name + "'");
                } else {
                    tm.emitComment("[ERROR] Undeclared variable: " + name);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(OpExp node, int level) {
        try {
            tm.emitComment("Binary Operation");
            node.left.accept(this, level);
            tm.emitRM("ST", 0, tempOffset, 6, "Push left operand to temp stack");
            node.right.accept(this, level);
            tm.emitRM("LD", 1, tempOffset, 6, "Load left operand into R1");

            switch (node.op) {
                case OpExp.PLUS:
                    tm.emitRO("ADD", 0, 1, 0, "R0 = R1 + R0"); break;
                case OpExp.MINUS:
                    tm.emitRO("SUB", 0, 1, 0, "R0 = R1 - R0"); break;
                case OpExp.TIMES:
                    tm.emitRO("MUL", 0, 1, 0, "R0 = R1 * R0"); break;
                case OpExp.OVER:
                case OpExp.DIVIDE:
                    tm.emitRO("DIV", 0, 1, 0, "R0 = R1 / R0"); break;
                case OpExp.EQ:
                    tm.emitRO("TEQ", 0, 1, 0, "R0 = R1 == R0"); break;
                case OpExp.NEQ:
                    tm.emitRO("TNE", 0, 1, 0, "R0 = R1 != R0"); break;
                case OpExp.LT:
                    tm.emitRO("TLT", 0, 1, 0, "R0 = R1 < R0"); break;
                case OpExp.GT:
                    tm.emitRO("TGT", 0, 1, 0, "R0 = R1 > R0"); break;
                case OpExp.LTE:
                    tm.emitRO("TLE", 0, 1, 0, "R0 = R1 <= R0"); break;
                case OpExp.GTE:
                    tm.emitRO("TGE", 0, 1, 0, "R0 = R1 >= R0"); break;
                default:
                    tm.emitComment("Unsupported operator: " + node.op);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void visit(DecList node, int level) {
        while (node != null) {
            if (node.head != null) node.head.accept(this, level);
            node = node.tail;
        }
    }

    @Override
    public void visit(FunctionDec node, int level) {
        try {
            tm.emitComment("Function " + node.func_name);
            if (node.body instanceof CompoundExp) {
                node.body.accept(this, level + 1);
            } else {
                tm.emitComment("[WARN] Function body is not a CompoundExp");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Stub methods for now
    @Override public void visit(ExpList exp, int level) {}
    @Override public void visit(IfExp exp, int level) {}
    @Override public void visit(ReadExp exp, int level) {}
    @Override public void visit(RepeatExp exp, int level) {}
    @Override public void visit(VarExp exp, int level) {}
    @Override public void visit(WriteExp exp, int level) {}
    @Override public void visit(ArrayDec exp, int level) {}
    @Override public void visit(BoolExp exp, int level) {}
    @Override public void visit(CompoundExp exp, int level) {}
    @Override public void visit(IndexVar exp, int level) {}
    @Override public void visit(NameTy exp, int level) {}
    @Override public void visit(SimpleDec exp, int level) {}
    @Override public void visit(SimpleVar exp, int level) {}
    @Override public void visit(VarDecList exp, int level) {}
    @Override public void visit(WhileExp exp, int level) {}
    @Override public void visit(NilExp exp, int level) {}
    @Override public void visit(CallExp exp, int level) {}
    @Override public void visit(ReturnExp exp, int level) {}
}
