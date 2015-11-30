package cc.openhome;

class Some {
    private int a = 10;
    private String text = "n.a.";
    public Some(int a)  {
        if(a > 0)   {
            this.a = a;
        }
    }
    public Some(int a, String text) {
        if(a > 0)   {
            this.a = a;
        }
        if(text != null)    {
            this.text = text;
        }
    }
    public int getA()  {
        return a;
    }
    
    String getText()    {
        return text;
    }
    
}
public class SomeApp {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        Some some1 = new Some(19);
        System.out.printf("%d%n", some1.getA());
        Some some2 = new Some(15, "vv");
        System.out.printf("%d, %s%n", some2.getA(), some2.getText());
        
    }

}
