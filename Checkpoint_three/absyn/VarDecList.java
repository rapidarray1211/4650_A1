package absyn;

public class VarDecList extends Absyn {
    public VarDec head;
    public VarDecList tail;
  
    public VarDecList( VarDec  head, VarDecList tail ) {
      this.head = head;
      this.tail = tail;
    }
  
    public void accept( AbsynVisitor visitor, int level, boolean flag ) {
      visitor.visit( this, level, flag );
    }
}
