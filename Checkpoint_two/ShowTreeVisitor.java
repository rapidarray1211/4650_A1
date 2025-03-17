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
	printer.indent(level);
	printer.printMsgNoNewLine("OpExp:");
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
        operator = " > ";;
        break;
      case OpExp.UMINUS:
        operator = " - ";
        break;
      default:
		printer.printMsg("Unrecognized operator at line " + exp.row + " and column " + exp.col);
    }
	printer.printMsg(operator);
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
    printer.indent(level);
    level++;
    printer.printMsg("VarExp:");
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
    printer.indent(level);
    level++;
    symbolCount++;

	printer.printMsg("Name: " + exp.name);
    if (exp.size != 0) {
	  printer.printLevel("Size: " + exp.size, level);
    }
    printer.indent(level);
    exp.type.accept(this, level);
    
  }

  public void visit( BoolExp exp, int level ) {
    printer.indent(level);
    level++;
	printer.printMsg( "BoolExp: " + exp.value );
  }

  public void visit(CompoundExp exp, int level) {
    printer.indent(level);
    level++;
	printer.printMsg( "CompoundExp:" );
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
    printer.indent(level);
    level++;
    symbolCount++;
	printer.printMsg("FunctionDec " + exp.func_name);
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
    printer.indent(level);
    level++;
	printer.printMsg("IndexVar " + exp.name);
    exp.index.accept(this, level);
  }

  public void visit( NameTy exp, int level ) {
    // indent(level);
    switch (exp.type) {
      case 0:
		printer.printMsg("NameTy: BOOLEAN");
        break;
      case 1:
		printer.printMsg("NameTy: INT");
        break;
      case 2:
		printer.printMsg("NameTy: VOID");
        break;
      case 3:
		printer.printMsg("NameTy: NULL");
        break;
    }
  }

  public void visit( SimpleDec exp, int level ) {
    printer.indent(level);
    level++;
    symbolCount++;
	printer.printMsg("SimpleDec: " + exp.name);
    printer.indent(level);
    if (exp.type != null) {
      exp.type.accept(this, level);
    }
  }

  public void visit( SimpleVar exp, int level ) {
    printer.indent(level);
    level++;
	printer.printMsg("SimpleVar: " + exp.name);
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
    printer.indent(level);
    level++;
	printer.printMsg("WhileExp");
    if (exp.test != null) {
      exp.test.accept(this, level);
    }
    if (exp.body != null) {
      exp.body.accept(this, level);
    }
  }

  public void visit( NilExp exp, int level ) {
    printer.indent(level);
    // level++;
	printer.printMsg("NiLExp");
  }

  public void visit( CallExp exp, int level ) {
    printer.indent(level);
    level++;
	printer.printMsg("CallExp: " + exp.func);
    if (exp.args != null) {
      exp.args.accept(this, level);
    }
  }

  public void visit( ReturnExp exp, int level) { 
    printer.indent(level);
    level++;
	printer.printMsg("ReturnExp");
    if (exp.exp != null) {
      exp.exp.accept(this, level);
    }
  }
}
