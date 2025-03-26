package absyn;

public interface AbsynVisitor {

  void visit(ExpList exp, int level, boolean flag);
  void visit(AssignExp exp, int level, boolean flag);
  void visit(IfExp exp, int level, boolean flag);
  void visit(IntExp exp, int level, boolean flag);
  void visit(OpExp exp, int level, boolean flag);
  void visit(ReadExp exp, int level, boolean flag);
  void visit(RepeatExp exp, int level, boolean flag);
  void visit(VarExp exp, int level, boolean flag);
  void visit(WriteExp exp, int level, boolean flag);
  void visit(ArrayDec exp, int level, boolean flag);
  void visit(BoolExp exp, int level, boolean flag);
  void visit(CompoundExp exp, int level, boolean flag);
  void visit(DecList exp, int level, boolean flag);
  void visit(FunctionDec exp, int level, boolean flag);
  void visit(IndexVar exp, int level, boolean flag);
  void visit(NameTy exp, int level, boolean flag);
  void visit(SimpleDec exp, int level, boolean flag);
  void visit(SimpleVar exp, int level, boolean flag);
  void visit(VarDecList exp, int level, boolean flag);
  void visit(WhileExp exp, int level, boolean flag);
  void visit(NilExp exp, int level, boolean flag);
  void visit(CallExp exp, int level, boolean flag);
  void visit(ReturnExp exp, int level, boolean flag);

}
