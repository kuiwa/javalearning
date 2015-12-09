package cc.openhome;

import java.util.*;

public class Average {

    public static void main(String[] args) {
        // TODO Auto-generated method stub
        try {
            Scanner scanner = new Scanner(System.in);
            int number;
            double sum = 0;
            int count = 0;
            while(true) {
                number = scanner.nextInt();
                if(number == 0) {
                    break;
                }
                sum += number;
                count++;
            }
            System.out.printf("Average value = %.2f%n", sum / count);
        }   catch (InputMismatchException ex) {
            System.out.println("must input a inter");
            //test git diff
        }
    }

}
