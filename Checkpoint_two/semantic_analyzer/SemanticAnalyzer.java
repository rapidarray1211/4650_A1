package semantic_analyzer;

import absyn.*;

// import java.util.List;
// import java.lang.classfile.components.ClassPrinter.Node;
import java.util.ArrayList;

public class SemanticAnalyzer implements AbsynVisitor {
    
    private SymbolTable symbolTable;
    public AnalyzerPrinter printer;
    private ArrayList<FunctionDec> currentFunction;
    private boolean isReturned = false; //keep track if function is returned or not

    public SemanticAnalyzer(int nSymbols, AnalyzerPrinter printer) {
        symbolTable = new SymbolTable(nSymbols);
        currentFunction = new ArrayList<>();
        this.printer = printer;
    }


    public void analyze(DecList root) {
        printer.printLevel("Entering global scope:", symbolTable.getScope());
        symbolTable.enterScope();
        visit(root, 0);
        printer.printLevelList(symbolTable.printScope(), symbolTable.getScope());
        symbolTable.delete();
        printer.printLevel("Leaving global scope:", symbolTable.getScope());
    }

    @Override
    public void visit(DecList node, int level) {
        
        Dec lastNode = null;
        while (node != null) {
            if (node.head != null) {
                node.head.accept(this, level);
            }
            lastNode = node.head;
            node = node.tail;
        }

        //Check if last node main
        if (lastNode instanceof FunctionDec lNode){
            if (!(lNode.name.equals("main"))){
                printer.printErr("[ERROR] Last function is not main.");
            }
        }
        else{
            printer.printErr("[ERROR] main function is not last");
        }
    }

    @Override
    public void visit(FunctionDec node, int level) {

        VarDecList params = node.parameters;

        NodeType nodeType = symbolTable.lookup(node.name);

        //Check for existing symbols
        if (nodeType != null){

            if (nodeType.def instanceof FunctionDec def){

                if (!(node.body instanceof NilExp)){
                    //if node is a prototype then replace
                    if (symbolTable.isPrototype(node, def)){
                        symbolTable.replacePrototype(node.name, node);
                    }
                    else if (nodeType.level == symbolTable.getScope()){
                        printer.printErr("[ERROR] " + node.row + ", " + node.col + " a function with the name " + node.name + " exists.");
                    }
                    else{
                        symbolTable.insert(node.name, node);
                    }
                }
                else{
                    printer.printErr("[ERROR] " + node.row + ", " + node.col + ", " + node.name + " is a duplicate prototype.");
                }
            }
            else if (nodeType.level == symbolTable.getScope()){
                printer.printErr("[ERROR] " + node.row + ", " + node.col + " a symbol with the name " + node.name + " exists.");
            }
            else{
                symbolTable.insert(node.name, node);
            }
        }
        else{
            symbolTable.insert(node.name, node);
        }

        //only enter scope if not a function prototype
        if (!(node.body instanceof NilExp)){

            currentFunction.add(node);
            isReturned = false;
    
            printer.printLevel("Entering scope of " + node.name + ":", symbolTable.getScope());
            symbolTable.enterScope();

            while (params != null && params.head != null) {
                params.head.accept(this, level + 1);
                params = params.tail;
            }
            
 
            node.body.accept(this, level + 1);

            if (!isReturned && (node.type.type == NameTy.INT || node.type.type == NameTy.BOOL)){
                printer.printErr("[ERROR] " + node.row + ", " + node.col + " function is not returned");
            }

            if (!(currentFunction.isEmpty())) currentFunction.remove(currentFunction.size() - 1);

            printer.printLevelList(symbolTable.printScope(), symbolTable.getScope());
            symbolTable.delete();
            printer.printLevel("Leaving the function scope:", symbolTable.getScope());
        }
        
    }
    

    @Override
    public void visit(SimpleDec node, int level) {

        if (symbolTable.isInScope(node.name)){
            printer.printErr("[ERROR] " + node.row + ", " + node.col + " a symbol with the name" + node.name + " exists.");
        }
        else{
            symbolTable.insert(node.name, node);
        }
    }

    @Override
    public void visit(ArrayDec node, int level) {
        if (symbolTable.isInScope(node.name)){
            printer.printErr("[ERROR] " + node.row + ", " + node.col + " a symbol with the name" + node.name + " exists.");
        }
        else{
            symbolTable.insert(node.name, node);
        }
    }

    @Override
    public void visit(CompoundExp node, int level) {

        node.dtype = null;

        if (node.decs != null) {
            node.decs.accept(this, level + 1);
        }

        if (node.exps != null) {

            node.exps.accept(this, level + 1);
        }
  
    }

    @Override
    public void visit(AssignExp node, int level) {     

        node.lhs.accept(this, level);
        node.rhs.accept(this, level);

        node.dtype = node.lhs.dtype;

        if (!isTypeCompatible(node.lhs, node.rhs)){
            printer.printErr("[ERROR] " + node.lhs.row + ", " + node.lhs.col + " Type mismatch") ;
        }

        

    }

    @Override
    public void visit(OpExp node, int level) {

        if (node.op == OpExp.PLUS || node.op == OpExp.DIVIDE || node.op == OpExp.MINUS 
            ||  node.op == OpExp.UMINUS ||  node.op == OpExp.TIMES){

            node.dtype = new SimpleDec(node.row, node.col, new NameTy(node.row, node.col, NameTy.INT), "Int_Op");
        }
        else{
            node.dtype = new SimpleDec(node.row, node.col, new NameTy(node.row, node.col, NameTy.BOOL), "Bool_Op");
        }
    
        node.left.accept(this, level);
        node.right.accept(this, level);
        if (!isTypeCompatible(node.left, node.right)) {
            printer.printErr("[ERROR] " + node.row + ", " + node.col + " Type mismatch");
        }
    }

    @Override
    public void visit(IfExp node, int level) {

        node.test.accept(this, level);

        if (node.test.dtype.getType().type != NameTy.BOOL){
            printer.printErr("[ERROR] " + node.row + ", " + node.col + " Not boolean expression in IF.");
        }

        printer.printLevel("Entering IF scope:", symbolTable.getScope());
        symbolTable.enterScope();

        node.thenpart.accept(this, level);

        printer.printLevelList(symbolTable.printScope(), symbolTable.getScope());
        symbolTable.delete();
        printer.printLevel("Leaving IF scope:", symbolTable.getScope());

        if (node.elsepart != null) {

            printer.printLevel("Entering ELSE scope:", symbolTable.getScope());
            symbolTable.enterScope();

            node.elsepart.accept(this, level);

            printer.printLevelList(symbolTable.printScope(), symbolTable.getScope());
            symbolTable.delete();
            printer.printLevel("Leaving ELSE scope:", symbolTable.getScope());
        }
    }

    @Override
    public void visit(WhileExp node, int level) {
        node.dtype = null;

        node.test.accept(this, level);

        if (node.test.dtype.getType().type != NameTy.BOOL){
            printer.printErr("[ERROR] " + node.row + ", " + node.col + " Not boolean expression in WHILE.");
        }

        printer.printLevel("Entering WHILE scope:", symbolTable.getScope());
        symbolTable.enterScope();

        node.body.accept(this, level);

        printer.printLevelList(symbolTable.printScope(), symbolTable.getScope());
        symbolTable.delete();
        printer.printLevel("Leaving WHILE scope:", symbolTable.getScope());

    }

    @Override
    public void visit(ReturnExp node, int level) {

        if (node.exp != null) {
            node.exp.accept(this, level);

            node.dtype = node.exp.dtype;
        }
        else{
            node.dtype = new SimpleDec(node.row, node.col,new NameTy(node.row, node.col, NameTy.VOID), "return");
        }

        if (!(currentFunction.isEmpty())){
            if (node.dtype.getType().type != currentFunction.get(currentFunction.size() - 1).type.type){
                printer.printErr("[ERROR] " + node.row + ", " + node.col + " Return type mismatch");
            }
            this.isReturned = true;
        }
        else{
            printer.printErr("[ERROR] " + node.row + ", " + node.col + " No function to return from.");
        }
    }


    @Override
    public void visit(CallExp node, int level) {

        NodeType entry = symbolTable.lookup(node.func);
        
        if (entry == null) {
            printer.printErr("[ERROR] " + node.row + ", " + node.col + " Undeclared function " + node.func);
            node.dtype = new SimpleDec(node.row, node.col,new NameTy(node.row, node.col, NameTy.NULL), node.func);
        }
        else if (entry.def instanceof FunctionDec def){
            node.dtype = def;
            ExpList args = node.args;
            VarDecList params = def.parameters;
            
            while (args != null && params != null){
                if (args.head != null && params.head != null){

                    args.head.accept(this, level);

                    if (args.head.dtype.getClass() != params.head.getClass()){
                        printer.printErr("[ERROR] " + node.row + ", " + node.col + " Unmatched parameters in function");
                        break;
                    }
                }

                args = args.tail;
                params = params.tail;
            }
            if (!(args == null && params == null)){
                printer.printErr("[ERROR] " + node.row + ", " + node.col + " Unmatched parameters in function");
            }
        }
        else{
            printer.printErr("[ERROR] " + node.row + ", " + node.col + " Undeclared function " + node.func);
            node.dtype = entry.def;
        }
    
    }
    

    @Override
    public void visit(VarExp node, int level) {
        NodeType entry = symbolTable.lookup(node.variable.getName());

        if (entry == null) {
            printer.printErr("[ERROR] "+ node.row + ", " + node.col + ", variable" + node.variable.getName() + "' is undeclared.");

            node.dtype = new SimpleDec(node.row,node.col, new NameTy(node.row,node.col,NameTy.NULL), node.variable.getName());
        }
        else{
            node.dtype = new SimpleDec(node.row,node.col, new NameTy(node.row,node.col,entry.def.getType().type), node.variable.getName());
        }
    }

    public void visit(IntExp node, int level) {
        node.dtype = new SimpleDec(node.row,node.col, new NameTy(node.row,node.col,NameTy.INT), node.value);
    }

    public void visit(BoolExp node, int level) {
        node.dtype = new SimpleDec(node.row,node.col, new NameTy(node.row,node.col,NameTy.BOOL), "bool");
    }

    public void visit(WriteExp node, int level) {
        node.output.accept(this, level);
    }

    public void visit(ReadExp node, int level) {
        node.input.accept(this, level);
    }

    public void visit(RepeatExp node, int level) {
        node.exps.accept(this, level);
        node.test.accept(this, level);
    }

    public void visit(ExpList node, int level) {
        printer.printErr("[debug] " + node.head.getClass());
        while (node != null && node.head != null) {
            
            if (node.head instanceof CompoundExp){
                //seperate scope entering for compound exp because also used in functions
                printer.printLevel("Entering new block:", symbolTable.getScope());
                symbolTable.enterScope();

                node.head.accept(this, level);

                printer.printLevelList(symbolTable.printScope(), symbolTable.getScope());
                symbolTable.delete();
                printer.printLevel("Leaving block:", symbolTable.getScope());

            }
            else{
              
                node.head.accept(this, level);
            }
            node = node.tail;
        }

    }

    public void visit(NilExp node, int level) {
        node.dtype = new SimpleDec(node.row, node.col,new NameTy(node.row, node.col, NameTy.NULL), "null");
    }

    public void visit(VarDecList node, int level) {
        while (node != null) {
            if (node.head != null) {
                node.head.accept(this, level);
            }
            node = node.tail;
        }
    }

    //non array variable decleration
    public void visit(SimpleVar node, int level) {
        NodeType entry = symbolTable.lookup(node.name);
        if (entry == null) {
            printer.printErr("[ERROR] " + node.row + ", " + node.col + " undeclared variable " + node.name);
        }
    }

    public void visit(NameTy node, int level) {
        if (node.type < 0 || node.type > 3) {
            printer.printErr("[ERROR] " + node.row + ", " + node.col + " Invalid type");
        }
    }

    public void visit(IndexVar node, int level) {
        NodeType entry = symbolTable.lookup(node.name);
        if (entry == null) {
            printer.printErr("[ERROR] " + node.row + ", " + node.col + " undeclared variable " + node.name);
        } 
        else if (!(entry.def instanceof ArrayDec)){
            printer.printErr("[ERROR] " + node.row + ", " + node.col + " " + node.name + " is not array.");
        }

        node.index.accept(this, level);

        if (node.index.dtype.getType().type != NameTy.INT ) {
            printer.printErr("[ERROR] '" + node.name + " is not an array.");
        }

    }

    private boolean isTypeCompatible(Exp left, Exp right) {
        return left.dtype.getType().type == right.dtype.getType().type;
    }

    private String getType(VarDec node) {
        if (node instanceof SimpleDec) return ((SimpleDec) node).type.toString();
        if (node instanceof ArrayDec) return ((ArrayDec) node).type.toString();
        return "unknown";
    }

    //placeholder function 
    public int getSymbolCount(){
        return 0;
    }



}
