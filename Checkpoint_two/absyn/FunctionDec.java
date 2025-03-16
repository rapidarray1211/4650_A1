package absyn;
public class FunctionDec extends Dec{
    public NameTy type; //return type
    public String name;
    public VarDecList parameters;
    public Exp body;

    public FunctionDec (int row, int col, NameTy returnType, String funcName, VarDecList parameters, Exp body) {
        this.row = row;
        this.col = col;
        this.type = returnType;
        this.name = funcName;
        this.parameters = parameters;
        this.body = body;
    }

    public void accept( AbsynVisitor visitor, int level ) {
        visitor.visit( this, level );
    }

}
