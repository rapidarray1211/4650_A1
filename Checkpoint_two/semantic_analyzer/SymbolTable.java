package semantic_analyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
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

    public void exitScope(){
        currentScope--;
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

    //Delete's entry based on current scope
    public int delete(){
        Iterator<Map.Entry<String, ArrayList<NodeType>>> iterator = table.entrySet().iterator();
        while (iterator.hasNext()){

            Map.Entry<String, ArrayList<NodeType>> entry = iterator.next();
            ArrayList<NodeType> nodeArray = entry.getValue();

            for (int i = nodeArray.size() - 1; i >= 0; i--){
                NodeType node = nodeArray.get(i);

                if (node.level == currentScope){
                    nodeArray.remove(i);
                }
                else{
                    break;
                }
            }


            if (nodeArray.isEmpty()){
                iterator.remove();
            }

        }

        return currentScope;
    }

}
