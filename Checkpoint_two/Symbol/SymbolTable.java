package Symbol;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import absyn.Dec;

public class SymbolTable {
    private HashMap<String, ArrayList<NodeType>> table;
    private int currentScope;

    public SymbolTable(int nSymbols) {
        int capacity = (int)(nSymbols * 1.5);
        this.table = new HashMap<>(capacity);
        this.currentScope = 0;
        System.out.println("[INIT] Symbol Table Created. Global Scope: " + currentScope);
    }


    public void enterScope() {
        currentScope++;
    }


    public boolean insert(String name, Dec type) {
        NodeType newNode = new NodeType(name, type, currentScope);
        ArrayList<NodeType> nodeArray = table.get(name);

        if (nodeArray == null) {
            nodeArray = new ArrayList<>();
            nodeArray.add(newNode);
            table.put(name,nodeArray);
        }
        else{
            nodeArray.add(newNode);
        }
        return true;
    }

    public NodeType lookup(String name) {
        ArrayList<NodeType> nodeArray = table.get(name);
        if (nodeArray == null) {
            return null;
        } else {
            return nodeArray.get(nodeArray.size() - 1);
        }
    }

    public int delete(){
        for (Map.Entry<String,ArrayList<NodeType>> entry : table.entrySet()){
            ArrayList<NodeType> nodeArray = entry.getValue();
            NodeType node = nodeArray.get(nodeArray.size() - 1);

            if (node.level == currentScope){
                nodeArray.remove(nodeArray.size() - 1);
            }

            if (nodeArray.isEmpty()){
                String name = entry.getKey();
                table.remove(name);
            }

        }
        
        currentScope --;
        return currentScope;
    }

}
