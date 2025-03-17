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
	printer.printLevel("AssignExp:", level);
    level++;
    exp.lhs.accept( this, level );
    exp.rhs.accept( this, level );
  }

  public void visit( IfExp exp, int level ) {
	printer.printLevel("IfExp:", level);
    level++;
    exp.test.accept( this, level );
    exp.thenpart.accept( this, level );
    if (exp.elsepart != null )
       exp.elsepart.accept( this, level );
  }

  public void visit( IntExp exp, int level ) {
	printer.printLevel("IntExp:", level);
  }


  public void visit( OpExp exp, int level ) {
	
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
      default:
		    printer.printLevelNoLn("Unrecognized operator at line " + exp.row + " and column " + exp.col, level);
    }
	  printer.printLevel(operator, level);
    level++;
    if (exp.left != null)
       exp.left.accept( this, level );
    if (exp.right != null)
       exp.right.accept( this, level );
  }

  public void visit( ReadExp exp, int level ) {
	printer.printLevel("ReadExp:", level);
    level++;
    exp.input.accept( this, ++level );
    if (exp.input != null) {
      exp.input.accept(this, level);
    }
  }

  public void visit( RepeatExp exp, int level ) {
    printer.printLevel("RepeatExp:", level);
    level++;
    ExpList exps = exp.exps;
    if (exps != null) {
      exps.accept(this, level);
    }
    if (exp.test != null) {
      exp.test.accept(this, level);
    }
  }

//Should level be in this order?
  public void visit( VarExp exp, int level ) {

    level++;
    printer.printLevel("VarExp:",level);
    if (exp.variable != null) {
      exp.variable.accept(this, level);
    }
  }

  public void visit( WriteExp exp, int level ) {
    printer.printLevel("WriteExp:", level);
    if (exp.output != null)
       exp.output.accept( this, ++level );
  }

  public void visit( ArrayDec exp, int level ) {

    level++;
    symbolCount++;

	printer.printLevel("Name: " + exp.name,level);
    if (exp.size != 0) {
	  printer.printLevel("Size: " + exp.size, level);
    }
    exp.type.accept(this, level);
    
  }

  public void visit( BoolExp exp, int level ) {
    //level++;
	  printer.printLevel( "BoolExp: " + exp.value , level);
  }

  public void visit(CompoundExp exp, int level) {

	  printer.printLevel( "CompoundExp:", level);
    level++;
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

    printer.printLevel("FunctionDec: " + exp.func_name, level);
    level++;
    symbolCount++;

    if (exp.return_type != null) {
	  printer.printLevel("RETURN: ", level);
      exp.return_type.accept(this, level);
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
	  printer.printLevel("IndexVar " + exp.name, level);
    level ++;
    exp.index.accept(this, level);
  }

  public void visit( NameTy exp, int level ) {
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

  public void visit( SimpleDec exp, int level ) {
    symbolCount++;
	  printer.printLevel("SimpleDec: " + exp.name, level);
    level++;
    printer.indent(level);
    if (exp.type != null) {
      exp.type.accept(this, level);
    }
  }

  public void visit( SimpleVar exp, int level ) {

  	printer.printLevel("SimpleVar: " + exp.name, level);

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
	  printer.printLevel("WhileExp", level);
    level ++;
    if (exp.test != null) {
      exp.test.accept(this, level);
    }
    if (exp.body != null) {
      exp.body.accept(this, level);
    }
  }

  public void visit( NilExp exp, int level ) {
    // level++;
	  printer.printLevel("NiLExp", level);
  }

  public void visit( CallExp exp, int level ) {
	  printer.printLevel("CallExp: " + exp.func, level);
    level++;
    if (exp.args != null) {
      exp.args.accept(this, level);
    }
  }

  public void visit( ReturnExp exp, int level) { 

	  printer.printLevel("ReturnExp",level);
    level++;
    if (exp.exp != null) {
      exp.exp.accept(this, level);
    }
  }
}