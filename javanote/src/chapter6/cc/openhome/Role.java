package cc.openhome;

//public class Role {
public abstract class Role {
    protected String name;
    protected int level;
    protected int blood;
    
    public int getBlood()  {
        return blood;
    }
    public void setBlood(int blood)   {
        this.blood = blood   ; 
    }
    public int getLevel()   {
        return level;
    }
    public void setLevel(int level)   {
        this.level = level;
    }
    public String getName() {
        return name;
    }
    public void setName(String name)    {
        this.name = name;
    }
    public abstract void fight();
    //public void fight()   {
    //}
    public String toString()    {
        return String.format("(%s, %d, %d)%n", this.name, this.level, this.blood);
    }
}
