package absyn;

public class NameTy extends Absyn{
    public final static int BOOL = 0;
    public final static int INT = 1;
    public final static int VOID = 2;
    public final static int NULL = 3;
    public int type;

    public NameTy(int row, int col, int type) {
        this.row = row;
        this.col = col;
        this.type = type;
    }
    public void accept( AbsynVisitor visitor, int level, boolean flag ) {
        visitor.visit( this, level, flag );
    }
	
	public String getTypeName(int type) {
		switch (type) {
			case NameTy.BOOL:
				return "BOOL";
			case NameTy.INT:
				return "INT";
			case NameTy.VOID:
				return "VOID";
			case NameTy.NULL:
				return "NULL";
			default:
				return "Unknown Type";
		}
	}
}
