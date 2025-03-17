import absyn.*;

public class ShowTreeVisitor implements AbsynVisitor {

  static final int SPACES = 4;
  int symbolCount = 0;

  //keep track of numbr of symbols to determine size of hash table
  public int getSymbolCount() {
    return this.symbolCount;
  }
  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  public void visit( ExpList expList, int level ) {
    while( expList != null ) {
      if (expList.head != null) {
        expList.head.accept( this, level );
        expList = expList.tail;
      }
      else {
        break;
      }
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
      case OpExp.DIVIDE:
        System.out.println( " / " );
        break;
      case OpExp.EQ:
        System.out.println( " == " );
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
    level++;
    exp.input.accept( this, ++level );
    if (exp.input != null) {
      exp.input.accept(this, level);
    }
  }

  public void visit( RepeatExp exp, int level ) {
    indent( level );
    System.out.println( "RepeatExp:" );
    level++;
    ExpList exps = exp.exps;
    if (exps != null) {
      exps.accept(this, level);
    }
    if (exp.test != null) {
      exp.test.accept(this, level);
    }
  }

  public void visit( VarExp exp, int level ) {
    indent( level );
    level++;
    System.out.println( "VarExp:" );
    if (exp.variable != null) {
      exp.variable.accept(this, level);
    }
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
    symbolCount++;

    System.out.println("Name: " + exp.name);
    if (exp.size != 0) {
      indent(level);
      System.out.println("Size: " + exp.size);
    }
    indent(level);
    exp.type.accept(this, level);
    
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
    symbolCount++;
    System.out.println("FunctionDec " + exp.name);
    if (exp.type != null) {
      indent(level);
      System.out.print("RETURN: ");
      exp.type.accept(this, level);
    }
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
    System.out.println("IndexVar: " + exp.name);
    exp.index.accept(this, level);
  }

  public void visit( NameTy exp, int level ) {
    // indent(level);
    switch (exp.type) {
      case 0:
        System.out.println("NameTy: BOOLEAN");
        break;
      case 1:
        System.out.println("NameTy: INT");
        break;
      case 2:
        System.out.println("NameTy: VOID");
        break;
      case 3:
        System.out.println("NameTy: NULL");
        break;
    }
  }

  public void visit( SimpleDec exp, int level ) {
    indent(level);
    level++;
    symbolCount++;
    System.out.println("SimpleDec: " + exp.name);
    indent(level);
    if (exp.type != null) {
      exp.type.accept(this, level);
    }
  }

  public void visit( SimpleVar exp, int level ) {
    indent(level);
    level++;
    System.out.println("SimpleVar: " + exp.name);
  }

  public void visit( VarDecList exp, int level ) {
    while( exp != null ) {
      if (exp.head != null) {
        exp.head.accept( this, level );
        exp = exp.tail;
      }
    } 
  }

  public void visit( WhileExp exp, int level ) {
    indent(level);
    level++;
    System.out.println("WhileExp");
    if (exp.test != null) {
      exp.test.accept(this, level);
    }
    if (exp.body != null) {
      exp.body.accept(this, level);
    }
  }

  public void visit( NilExp exp, int level ) {
    indent(level);
    // level++;
    System.out.println("NiLExp");
  }

  public void visit( CallExp exp, int level ) {
    indent(level);
    level++;
    System.out.println("CallExp: " + exp.func);
    if (exp.args != null) {
      exp.args.accept(this, level);
    }
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
