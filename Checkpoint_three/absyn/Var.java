package absyn;

public abstract class Var extends Absyn {

    //link to the vardec
    public VarDec vardec;

    public void setVarDec(VarDec variable){
        this.vardec = variable;
    }

    public VarDec getVarDec(){
        return this.vardec;
    }
}
