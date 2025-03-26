import absyn.*;
import Symbol.AnalyzerPrinter;

public class ShowTreeVisitor implements AbsynVisitor {

  static final int SPACES = 4;
  int symbolCount = 0;
  private AnalyzerPrinter printer;
  
  public ShowTreeVisitor(AnalyzerPrinter printer) {
        this.printer = printer;
    }

  //keep track of numbr of symbols to determine size of hash table
  public int getSymbolCount() {
    return this.symbolCount;
  }
  
  //Maybe don't need
  private void indent( int level ) {
    for( int i = 0; i < level * SPACES; i++ ) System.out.print( " " );
  }

  public void visit( ExpList expList, int level, boolean flag ) {
    while( expList != null ) {
      if (expList.head != null) {
        expList.head.accept(this, level, flag );
        expList = expList.tail;
      }
      else {
        break;
      }
    } 
  }

  public void visit( AssignExp exp, int level, boolean flag ) {
	printer.printLevel("AssignExp:", level);
    level++;
    exp.lhs.accept(this, level, flag );
    exp.rhs.accept(this, level, flag );
  }

  public void visit( IfExp exp, int level, boolean flag ) {
	printer.printLevel("IfExp:", level);
    level++;
    exp.test.accept(this, level, flag );
    exp.thenpart.accept(this, level, flag );
    if (exp.elsepart != null )
       exp.elsepart.accept(this, level, flag );
  }

  public void visit( IntExp exp, int level, boolean flag ) {
	printer.printLevel("IntExp: " + exp.value, level);
  }


  public void visit( OpExp exp, int level, boolean flag ) {
	
	String operator = "";

	printer.printLevelNoLn("OpExp:",level);
    switch( exp.op ) {
      case OpExp.PLUS:
        operator = " + ";
        break;
      case OpExp.MINUS:
        operator = " - ";
        break;
      case OpExp.TIMES:
        operator = " * ";
        break;
      case OpExp.DIVIDE:
        operator = " / ";
        break;
      case OpExp.EQ:
        operator = " == ";
        break;
      case OpExp.LT:
        operator = " < ";
        break;
      case OpExp.GT:
        operator = " > ";
        break;
      case OpExp.UMINUS:
        operator = " - ";
        break;
      case OpExp.NOT:
        operator = " ~ ";
      default:
		    printer.printLevelNoLn("Unrecognized operator at line " + exp.row + " and column " + exp.col, level);
    }
	  printer.printMsg(operator);
    level++;
    if (exp.left != null)
       exp.left.accept(this, level, flag );
    if (exp.right != null)
       exp.right.accept(this, level, flag );
  }

  public void visit( ReadExp exp, int level, boolean flag ) {
	printer.printLevel("ReadExp:", level);
    level++;
    exp.input.accept( this, ++level, flag );
    if (exp.input != null) {
      exp.input.accept(this, level, flag);
    }
  }

  public void visit( RepeatExp exp, int level, boolean flag ) {
    printer.printLevel("RepeatExp:", level);
    level++;
    ExpList exps = exp.exps;
    if (exps != null) {
      exps.accept(this, level, flag);
    }
    if (exp.test != null) {
      exp.test.accept(this, level, flag);
    }
  }

//Should level be in this order?
  public void visit( VarExp exp, int level, boolean flag ) {
    printer.printLevel("VarExp:",level);
    level++;
    if (exp.variable != null) {
      exp.variable.accept(this, level, flag);
    }
  }

  public void visit( WriteExp exp, int level, boolean flag ) {
    printer.printLevel("WriteExp:", level);
    if (exp.output != null)
       exp.output.accept( this, ++level, flag );
  }

  public void visit( ArrayDec exp, int level, boolean flag ) {

    // level++;
    symbolCount++;
    printer.printLevel("ArrayDec: " + exp.name, level);
    if (exp.size != 0) {
      level++;
	    printer.printLevel("Size: " + exp.size, level);
      // level--;
    }
    exp.type.accept(this, level, flag);
    
  }

  public void visit( BoolExp exp, int level, boolean flag ) {
    //level++;
	  printer.printLevel( "BoolExp: " + exp.value , level);
  }

  public void visit(CompoundExp exp, int level, boolean flag) {

	  printer.printLevel( "CompoundExp:", level);
    level++;
    if (exp.decs != null) {
        VarDecList list1 = exp.decs;
        while (list1 != null) {
            if (list1.head != null) { 
                list1.head.accept(this, level, flag);
            }
            list1 = list1.tail;
        }
    }
    if (exp.exps != null) {
        ExpList list2 = exp.exps;
        while (list2 != null) {
            if (list2.head != null) {
                list2.head.accept(this, level, flag);
            }
            list2 = list2.tail;
        }
    }
}


  public void visit( DecList exp, int level, boolean flag ) {
    while ( exp != null && exp.head != null) {
        exp.head.accept(this, level, flag );
        exp = exp.tail;
    }
  }

  public void visit( FunctionDec exp, int level, boolean flag ) {

    printer.printLevel("FunctionDec: " + exp.func_name, level);
    level++;
    symbolCount++;

    if (exp.return_type != null) {
	  printer.printLevel("RETURN: ", level);
      exp.return_type.accept(this, level, flag);
    }
    exp.parameters.accept(this, level, flag);
    // while (exp.parameters != null && exp.parameters.head != null ) {
    //   exp.parameters.head.accept(this, level, flag);
    //   exp.parameters = exp.parameters.tail;
    // }
    if (exp.body != null) {
      exp.body.accept(this, level, flag);
    }
  }

  public void visit( IndexVar exp, int level, boolean flag ) {
	  printer.printLevel("IndexVar " + exp.name, level);
    level ++;
    exp.index.accept(this, level, flag);
  }

  public void visit( NameTy exp, int level, boolean flag ) {
    // indent(level);
    switch (exp.type) {
      case 0:
		printer.printLevel("NameTy: BOOLEAN", level);
        break;
      case 1:
		printer.printLevel("NameTy: INT", level);
        break;
      case 2:
		printer.printLevel("NameTy: VOID", level);
        break;
      case 3:
		printer.printLevel("NameTy: NULL", level);
        break;
    }
  }

  public void visit( SimpleDec exp, int level, boolean flag ) {
    symbolCount++;
	  printer.printLevel("SimpleDec: " + exp.name, level);
    level++;
    // printer.indent(level);
    if (exp.type != null) {
      exp.type.accept(this, level, flag);
    }
  }

  public void visit( SimpleVar exp, int level, boolean flag ) {

  	printer.printLevel("SimpleVar: " + exp.name, level);

  }

  public void visit( VarDecList exp, int level, boolean flag ) {
    printer.printLevel("VarDecList: ", level);
    level++;
    while( exp != null ) {
      if (exp.head != null) {
        exp.head.accept(this, level, flag );
      }
      exp = exp.tail;
    } 
  }

  public void visit( WhileExp exp, int level, boolean flag ) {
	  printer.printLevel("WhileExp", level);
    level ++;
    if (exp.test != null) {
      exp.test.accept(this, level, flag);
    }
    if (exp.body != null) {
      exp.body.accept(this, level, flag);
    }
  }

  public void visit( NilExp exp, int level, boolean flag ) {
    // level++;
	  printer.printLevel("NiLExp", level);
  }

  public void visit( CallExp exp, int level, boolean flag ) {
	  printer.printLevel("CallExp: " + exp.func, level);
    level++;
    if (exp.args != null) {
      exp.args.accept(this, level, flag);
    }
  }

  public void visit( ReturnExp exp, int level, boolean flag) { 

	  printer.printLevel("ReturnExp",level);
    level++;
    if (exp.exp != null) {
      exp.exp.accept(this, level, flag);
    }
  }
}