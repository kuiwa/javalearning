package cc.openhome;


public class Magician extends Role  {
    public void fight() {
        System.out.println(" magically attack");
    }
    public void cure()  {
        System.out.println(" cure");
    }
    public String toString()    {
        return "Magician" + super.toString();
    }
}
