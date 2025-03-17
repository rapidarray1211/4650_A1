package semantic_analyzer;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;
import absyn.*;


public class SymbolTable {
    private HashMap<String, ArrayList<NodeType>> table;
    private int currentScope;

    public SymbolTable(int nSymbols) {
        int capacity = (int)(nSymbols * 1.5);
        this.table = new HashMap<>(capacity);
        this.currentScope = 0;
    }

    public int getScope(){
        return currentScope;
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

    //get the most recent additon of a symbol
    public NodeType lookup(String name) {
        ArrayList<NodeType> nodeList = table.get(name);
        if (nodeList == null) {
            return null;
        } else {
            for (int i = nodeList.size() - 1; i >= 0; i--){

                NodeType node = nodeList.get(i);
    
                if (node.name.equals(name)){
                    return node;
                }
            }
            return null;
        }
    }

    //Looks up NodeType of current Scope, for checking same var dec in current scope.
    public boolean isExist(NodeType getNode) {
        ArrayList<NodeType> nodeList= table.get(getNode.name);
        if (nodeList == null) {
            return false;
        } else {
            for (int i = nodeList.size() - 1; i >= 0; i--){

                NodeType node = nodeList.get(i);
    
                if (node.name.equals(getNode.name)){
                    return true;
                }
            }
            return false;
        }
    }

    //check if a name is in the scope
    public boolean isInScope (String name){
        NodeType node = lookup(name);

        if (node == null){
            return false;
        }
       
        return (node.level == currentScope);

    }

    //cehck if a name is declared in the scope

    //Checks if function 2 is a prototype of function 1
    public boolean isPrototype(FunctionDec dec1, FunctionDec dec2){

        //check body to determine if prototype
        if (!(dec2.body instanceof NilExp)){
            return false;
        }

        //compare names and parameters for equality
        if (dec1.name.equals(dec2.name)){
            VarDecList param1 = dec1.parameters;
            VarDecList param2 = dec2.parameters;

            while (param1 != null && param2 != null){
                VarDec head1 = param1.head;
                VarDec head2 = param2.head;

                if (head1 instanceof SimpleDec h1 && head2 instanceof SimpleDec h2){
                    
                    if (h1.type != h2.type){
                        return false;
                    }
                }
                else if (head1 instanceof ArrayDec h1 && head2 instanceof ArrayDec h2){

                    if (h1.type != h2.type){
                        return false;
                    }
                }
                else{
                    return false;
                }

                param1 = param1.tail;
                param2 = param2.tail;
            }

            return (param1 == null && param2 == null);

        }
        else{
            return false;
        }

    }

    //replaces a function prototype with a function
    public boolean replacePrototype(String name, FunctionDec fNode){
        ArrayList<NodeType> nodeList = table.get(name);

        if (nodeList == null){
            return false;
        }

        for (int i = nodeList.size() - 1; i >= 0; i--){

            NodeType node = nodeList.get(i);

            if (node.name.equals(fNode.name) && node.def instanceof FunctionDec){
                    node.def = fNode;
                    return true;
                }
        }

        return false;

    }

    //Delete's all entries based on current scope
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
        currentScope --;

        return currentScope;
    }

    public ArrayList<String> printScope(){

        ArrayList<String> output = new ArrayList<>();
        for (Map.Entry<String,ArrayList<NodeType>> entry : table.entrySet()){

            ArrayList<NodeType> nodeArray = entry.getValue();

            for (int i = nodeArray.size() - 1; i >= 0; i--){
                NodeType node = nodeArray.get(i);
                if (node.level == currentScope){
                    Dec def = node.def;

                    if (def instanceof FunctionDec){
                        VarDecList parameters = ((FunctionDec)def).parameters;
                        String funcName = ((FunctionDec)def).name;
                        String returnType = typeToStr(((FunctionDec)def).type);
                        String parameterList = "(";
                        VarDec head = parameters != null ? parameters.head : null;
                        while (head != null){
                            
                            if (head instanceof SimpleDec){
                                parameterList = parameterList.concat(typeToStr(((SimpleDec)head).type));
                            }
                            else if (head instanceof ArrayDec){
                                parameterList = parameterList.concat(typeToStr(((ArrayDec)head).type) + "[]");
                            }
                            else{
                                parameterList = parameterList.concat("Unknown Type");
                            }

                            parameters = parameters.tail;
                            head = parameters.head;
                            if (head != null){
                                parameterList = parameterList.concat(", ");
                            }
                        }
                        parameterList = parameterList.concat(")");
                        output.add(funcName + ": " + parameterList + " -> " + returnType);

                    }
                    else if (def instanceof SimpleDec){
                        String name = ((SimpleDec)def).name;
                        String type = typeToStr(((SimpleDec)def).type);
                        output.add(name + ": " + type);
                    }
                    else if (def instanceof ArrayDec){
                        String name = ((ArrayDec)def).name;
                        String type = typeToStr(((ArrayDec)def).type);
                        String size = String.valueOf(((ArrayDec)def).size);
                        output.add(name + ": " + type + "[" + size + "]");
                    }
                    else{
                        output.add("Unknown symbol");
                    }
                } else{
                    break;
                }
            }
        }
        return output;
    }

    //Gets the string type of NameTy
    public String typeToStr(NameTy type){
        switch (type.type) {
            case 0:
                return "bool";
            case 1:
                return "int";
            case 2:
                return "void";
            case 3:
                return "null";
            default:
                return "Unknown type";
        }
    }

}
