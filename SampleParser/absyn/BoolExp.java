package absyn;

public class BoolExp {
    public boolean value;

    public BoolExp( int row, int col, Boolean value ) {
      this.row = row;
      this.col = col;
      this.value = value;
    }
  
    public void accept( AbsynVisitor visitor, int level ) {
      visitor.visit( this, level );
    }
}
