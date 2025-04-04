package absyn;

public class ArrayDec extends VarDec{
    public String name;
    public int size;
    public NameTy type;

    public ArrayDec ( int row, int col, NameTy type, String name, int size ) {
        this.row = row;
        this.col = col;
        this.type = type;
        this.name = name;
        this.size = size;
    }

    public void accept( AbsynVisitor visitor, int level, boolean flag ) {
        visitor.visit( this, level, flag );
    }
}
