package Symbol;
import java.util.List;
public class SymbolEntry {
    public int scope;     // Scope level (0 = global, 1 = function, etc.)
    public int type;      // Data type (e.g., INT = 0, VOID = 1)
    public int dim;
    public int offset;    // Memory offset for storage
    public int pc;
    List <Integer> paramTypes;
    List <Integer> paramDims;

    public SymbolEntry(int type, int scope, int dim, int offset, int pc, List<Integer> paramTypes, List<Integer> paramDims) {
        this.type = type;
        this.scope = scope;
        this.dim = dim;
        this.offset = offset;
        this.pc = pc;
        this.paramTypes = paramTypes;
        this.paramDims = paramDims;
    }

    public SymbolEntry(int type, int scope, int dimensions, int offset, int pc) {
        this.scope = scope;
        this.type = type;
        this.dim = dimensions;
        this.offset = offset;
        this.pc = pc;
        this.paramTypes = null;
        this.paramDims = null;
    }

    @Override
    public String toString() {
        return "Entry{type=" + getTypeAsString(this.type) + 
               ", scope=" + this.scope + 
               ", dim=" + this.dim + 
               ", offset=" + this.offset + 
               ", pc=" + this.pc + "}";
    }
    
    private String getTypeAsString(int type) {
        switch (type) {
            case 0: return "bool";
            case 1: return "int";
            case 2: return "void";
            default: return "unknown";
        }
    }
    
}
