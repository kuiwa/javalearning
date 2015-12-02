package cc.openhome;

import java.util.Scanner;

public class Guest {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        ArrayList List = new ArrayList();
        Scanner scanner = new Scanner(System.in);
        String name;
        while(true) {
            System.out.print("Enter visitor name: ");
            name = scanner.nextLine();
            if(name.equals("quit")) {
                break;
            }
            List.add(name);
        }
        System.out.println("print visitor list:");
        for(int i=0; i < List.size(); i++)    {
            String guest = (String) List.get(i);
            System.out.println(guest.toUpperCase());
        }
    }

}
