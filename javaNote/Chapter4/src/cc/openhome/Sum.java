package cc.openhome;
import java.util.Scanner;

public class Sum {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner scanner = new Scanner(System.in);
		long number = 0;
		long sum = 0;
		do	{
			System.out.print("Enter a number: ");
			number = Long.parseLong(scanner.nextLine());
			sum += number;
		}	while(number != 0);
		System.out.println("Sum: " + sum);
		
	}

}
