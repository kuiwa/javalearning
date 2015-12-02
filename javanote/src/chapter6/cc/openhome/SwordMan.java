package cc.openhome;


public class SwordMan extends Role  {
    public void fight() {
        System.out.println(" fight");
    }
    public String toString()  {
        return "SwordMan" + super.toString();
    }
}
