package absyn;
public class FunctionDec extends Dec{
    public NameTy return_type;
    public String func_name;
    public VarDecList parameters;
    public Exp body;

    public FunctionDec (int row, int col, NameTy return_type, String func_name, VarDecList parameters, Exp body) {
        this.row = row;
        this.col = col;
        this.return_type = return_type;
        this.func_name = func_name;
        this.parameters = parameters;
        this.body = body;
    }

    public void accept( AbsynVisitor visitor, int level ) {
        // visitor.visit( this, level );
    }
}
