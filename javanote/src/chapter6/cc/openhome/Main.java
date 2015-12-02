package cc.openhome;

class Some  {
    void doService()    {
        System.out.println("some service");
    }
}
class Other extends Some    {
    @Override
    void doService()    {
        System.out.println("other service");
    }
}

public class Main {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Other other = new Other();
        other.doService();
    }

}
