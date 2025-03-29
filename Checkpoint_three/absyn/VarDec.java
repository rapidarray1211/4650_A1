package absyn;

public abstract class VarDec extends Dec{

    public int nestLevel;
    public int offset;

    public void setNestLevel(int level){
        this.nestLevel = level;
    }

    public int getNestLevel(){
        return this.nestLevel;
    }

    public void setOffset(int offset){
        this.offset = offset;
    }

    public int getOffset(){
        return this.offset;
    }
    
}
