package codegen;

import java.io.*;

public class TMWriter {
    private final BufferedWriter writer;
    private int currentLoc = 0;

    public TMWriter(Writer out) {
        this.writer = new BufferedWriter(out);
    }

    public void emitComment(String comment) throws IOException {
        writer.write("* " + comment + "\n");
    }

    public void emitRM(String opcode, int r, int d, int s, String comment) throws IOException {
        writer.write(String.format("%3d:  %-4s  %d,%d(%d)", currentLoc++, opcode, r, d, s));
        if (comment != null && !comment.isEmpty()) {
            writer.write("     * " + comment);
        }
        writer.write("\n");
    }

    public void emitRO(String opcode, int r, int s1, int s2, String comment) throws IOException {
        writer.write(String.format("%3d:  %-4s  %d,%d,%d", currentLoc++, opcode, r, s1, s2));
        if (comment != null && !comment.isEmpty()) {
            writer.write("     * " + comment);
        }
        writer.write("\n");
    }

    public void emitLabel(int label) throws IOException {
        writer.write(String.format("L%d:\n", label));
    }

    public void emitGotoLabel(String label) throws IOException {
        writer.write(String.format("    JMP %s\n", label));
    }

    public void emitRM_Abs(String opcode, int r, int a, String comment) throws IOException {
        writer.write(String.format("%3d:  %-4s  %d,%d(0)", currentLoc++, opcode, r, a));
        if (comment != null && !comment.isEmpty()) {
            writer.write("     * " + comment);
        }
        writer.write("\n");
    }

    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

    public int getCurrentLoc() {
        return currentLoc;
    }

    public void setCurrentLoc(int loc) {
        this.currentLoc = loc;
    }
}
