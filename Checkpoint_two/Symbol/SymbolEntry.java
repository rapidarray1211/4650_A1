package Symbol;
public class SymbolEntry {
    int scope;     // Scope level (0 = global, 1 = function, etc.)
    int type;      // Data type (e.g., INT = 0, VOID = 1)
    int dim;
    int offset;    // Memory offset for storage
    int pc;

    public SymbolEntry(int type, int scope, int dimensions, int offset, int pc) {
        this.scope = scope;
        this.type = type;
        this.dim = dimensions;
        this.offset = offset * dimensions;
        this.pc = pc;
    }

    @Override
    public String toString() {
        return "Entry{type=" + type + ", scope=" + scope + ", dim=" + dim + ", offset=" + offset + ", pc=" + pc + "}";
    }
}
