import absyn.*;

public class ShowTreeVisitor implements AbsynVisitor {

  final static int SPACES = 4;

  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  public void visit( ExpList expList, int level ) {
    while( expList != null ) {
      expList.head.accept( this, level );
      expList = expList.tail;
    } 
  }

  public void visit( AssignExp exp, int level ) {
    indent( level );
    System.out.println( "AssignExp:" );
    level++;
    exp.lhs.accept( this, level );
    exp.rhs.accept( this, level );
  }

  public void visit( IfExp exp, int level ) {
    indent( level );
    System.out.println( "IfExp:" );
    level++;
    exp.test.accept( this, level );
    exp.thenpart.accept( this, level );
    if (exp.elsepart != null )
       exp.elsepart.accept( this, level );
  }

  public void visit( IntExp exp, int level ) {
    indent( level );
    System.out.println( "IntExp: " + exp.value ); 
  }


  public void visit( OpExp exp, int level ) {
    indent( level );
    System.out.print( "OpExp:" ); 
    switch( exp.op ) {
      case OpExp.PLUS:
        System.out.println( " + " );
        break;
      case OpExp.MINUS:
        System.out.println( " - " );
        break;
      case OpExp.TIMES:
        System.out.println( " * " );
        break;
      case OpExp.OVER:
        System.out.println( " / " );
        break;
      case OpExp.EQ:
        System.out.println( " = " );
        break;
      case OpExp.LT:
        System.out.println( " < " );
        break;
      case OpExp.GT:
        System.out.println( " > " );
        break;
      case OpExp.UMINUS:
        System.out.println( " - " );
        break;
      default:
        System.out.println( "Unrecognized operator at line " + exp.row + " and column " + exp.col);
    }
    level++;
    if (exp.left != null)
       exp.left.accept( this, level );
    if (exp.right != null)
       exp.right.accept( this, level );
  }

  public void visit( ReadExp exp, int level ) {
    indent( level );
    System.out.println( "ReadExp:" );
    exp.input.accept( this, ++level );
  }

  public void visit( RepeatExp exp, int level ) {
    indent( level );
    System.out.println( "RepeatExp:" );
    level++;
    exp.exps.accept( this, level );
    exp.test.accept( this, level ); 
  }

  public void visit( VarExp exp, int level ) {
    indent( level );
    System.out.println( "VarExp: " + exp.name );
  }

  public void visit( WriteExp exp, int level ) {
    indent( level );
    System.out.println( "WriteExp:" );
    if (exp.output != null)
       exp.output.accept( this, ++level );
  }

  public void visit( ArrayDec exp, int level ) {
    indent( level );
    level++;
    switch (exp.type.type) {
      case 0:
        System.out.println("NameTy: bool");
        indent(level);
        System.out.println("Name: " + exp.name);
      case 1:
        System.out.println("NameTy: int");
        indent(level);
        System.out.println("Name: " + exp.name);
      case 2:
        System.out.println("NameTy: void");
        indent(level);
        System.out.println("Name: " + exp.name);
    }
    if (exp.size != 0) {
      indent(level);
      System.out.println("Size: " + exp.size);
    }
  }

  public void visit( BoolExp exp, int level ) {
    indent( level );
    level++;
    System.out.println( "BoolExp: " + exp.value );
  }

  public void visit(CompoundExp exp, int level) {
    indent(level);
    level++;
    System.out.println("CompoundExp:");
    if (exp.decs != null) {
        VarDecList list1 = exp.decs;
        while (list1 != null) {
            if (list1.head != null) { 
                list1.head.accept(this, level);
            }
            list1 = list1.tail;
        }
    }
    if (exp.exps != null) {
        ExpList list2 = exp.exps;
        while (list2 != null) {
            if (list2.head != null) {
                list2.head.accept(this, level);
            }
            list2 = list2.tail;
        }
    }
}


  public void visit( DecList exp, int level ) {
    while ( exp != null && exp.head != null) {
        exp.head.accept( this, level );
        exp = exp.tail;
    }
  }

  public void visit( FunctionDec exp, int level ) {
    indent(level);
    level++;
    System.out.println("FunctionDec");
    while (exp.parameters != null && exp.parameters.head != null ) {
      exp.parameters.head.accept(this, level);
      exp.parameters = exp.parameters.tail;
    }
    if (exp.body != null) {
      exp.body.accept(this, level);
    }
  }

  public void visit( IndexVar exp, int level ) {
    indent(level);
    level++;
    System.out.println("IndexVar");
  }

  public void visit( NameTy exp, int level ) {
    indent(level);
    level++;
    System.out.println("NameTy");
  }

  public void visit( SimpleDec exp, int level ) {
    indent(level);
    level++;
    System.out.println("SimpleDec");
  }

  public void visit( SimpleVar exp, int level ) {
    indent(level);
    level++;
    System.out.println("SimpleVar");
  }

  public void visit( VarDecList exp, int level ) {
    indent(level);
    level++;
    System.out.println("VarDecList");
  }

  public void visit( WhileExp exp, int level ) {
    indent(level);
    level++;
    System.out.println("WhileExp");
  }

  public void visit( NilExp exp, int level ) {
    indent(level);
    level++;
    System.out.println("NiLExp");
  }

  public void visit( CallExp exp, int level ) {
    indent(level);
    level++;
    System.out.println("CallExp");
  }

  public void visit( ReturnExp exp, int level) { 
    indent(level);
    level++;
    System.out.println("ReturnExp");
    if (exp.exp != null) {
      exp.exp.accept(this, level);
    }
  }
}
