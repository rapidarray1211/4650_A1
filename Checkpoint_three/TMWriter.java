import java.io.*;

public class TMWriter {
    private final BufferedWriter writer;
    private int emitLoc = 0;
    private int highEmitLoc = 0;

    public TMWriter(Writer out) {
        this.writer = new BufferedWriter(out);
    }

    public void emitComment(String comment) throws IOException {
        writer.write("* " + comment + "\n");
    }

    public void emitRM(String opcode, int r, int d, int s, String comment) throws IOException {
        writer.write(String.format("%3d:  %-5s %d, %d(%d)", emitLoc, opcode, r, d, s));
        if (comment != null && !comment.isEmpty()) {
            writer.write("\t* " + comment);
        }
        writer.write("\n");
        emitLoc++;
        if (highEmitLoc < emitLoc) highEmitLoc = emitLoc;
    }

    public void emitRO(String opcode, int r, int s, int t, String comment) throws IOException {
        writer.write(String.format("%3d:  %-5s %d, %d, %d", emitLoc, opcode, r, s, t));
        if (comment != null && !comment.isEmpty()) {
            writer.write("\t* " + comment);
        }
        writer.write("\n");
        emitLoc++;
        if (highEmitLoc < emitLoc) highEmitLoc = emitLoc;
    }

    public void emitRM_Abs(String opcode, int r, int a, String comment) throws IOException {
        int relativeAddr = a - (emitLoc + 1);
        writer.write(String.format("%3d:  %-5s %d, %d(%d)", emitLoc, opcode, r, relativeAddr, 7));
        if (comment != null && !comment.isEmpty()) {
            writer.write("\t* " + comment);
        }
        writer.write("\n");
        emitLoc++;
        if (highEmitLoc < emitLoc) highEmitLoc = emitLoc;
    }

    public int emitSkip(int distance) {
        int i = emitLoc;
        emitLoc += distance;
        if (highEmitLoc < emitLoc) highEmitLoc = emitLoc;
        return i;
    }

    public void emitBackup(int loc) throws IOException {
        if (loc > highEmitLoc) {
            emitComment("BUG in emitBackup: loc > highEmitLoc");
        }
        emitLoc = loc;
    }

    public void emitRestore() {
        emitLoc = highEmitLoc;
    }

    public void emitLabel(int label) throws IOException {
        writer.write(String.format("L%d:\n", label));
    }

    public void emitGotoLabel(String label) throws IOException {
        writer.write(String.format("    JMP %s\n", label));
    }

    public void close() throws IOException {
        writer.flush();
        writer.close();
    }

    public int getCurrentLoc() {
        return emitLoc;
    }

    public void setCurrentLoc(int loc) {
        this.emitLoc = loc;
    }

    public int getHighEmitLoc() {
        return highEmitLoc;
    }
}
