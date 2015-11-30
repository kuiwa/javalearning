package cc.openhome;

import java.util.Scanner;

public class CashApp {
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        CashCard[] cards = {
                new CashCard("A001", 500, 0),
                new CashCard("A002", 300, 0),
                new CashCard("A003", 1000, 0)
        };
        
        Scanner scanner = new Scanner(System.in);
        for(CashCard card : cards)   {
            System.out.printf(" store for (%s, %d, %d)", card.getNumber(), card.getBalance(), card.getBonus());
            card.store(scanner.nextInt());
            System.out.printf(" detail: (%s, %d, %d)%n", card.getNumber(), card.getBalance(), card.getBonus());
            System.out.printf("%d%n", card.getBalance());
            //System.out.printf("%d%n", card.getBalance());
        }
    }

}
