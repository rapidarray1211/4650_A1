package absyn;

public class OpExp extends Exp {
  public static final int PLUS   = 0;
  public static final int MINUS  = 1;
  public static final int TIMES  = 2;
  public static final int OVER   = 3;
  public static final int EQ     = 4;
  public static final int LT     = 5;
  public static final int GT     = 6;
  public static final int UMINUS = 7;
  public static final int LTE    = 8;
  public static final int GTE    = 9;
  public static final int NEQ    = 10;
  public static final int OR     = 11;
  public static final int AND    = 12;
  public static final int NOT    = 13;
  public static final int DIVIDE = 14;

  public Exp left;
  public int op;
  public Exp right;

  public OpExp( int row, int col, Exp left, int op, Exp right ) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.op = op;
    this.right = right;
  }

  public void accept( AbsynVisitor visitor, int level ) {
    visitor.visit( this, level );
  }
}
