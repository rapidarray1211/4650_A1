import absyn.Dec;

public class NodeType {
    public String name;
    public Dec def;
    public int level;

    // Constructor
    public NodeType(String name, Dec def, int level) {
        this.name = name;
        this.def = def;
        this.level = level;
    }
}
