package absyn;

public class BoolExp extends Exp {
    public boolean value;

    public BoolExp( int row, int col, String value ) {
      this.row = row;
      this.col = col;

      if (value.equals("true"))
        this.value = true;
      else if (value.equals("false"))
        this.value = false;
    }
  
    public void accept( AbsynVisitor visitor, int level ) {
      visitor.visit( this, level );
    }
}
