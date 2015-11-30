package cc.openhome;

import java.util.Arrays;

public class CopyArray {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int[] score1 = {88, 86, 90, 49};
		int[] score2 = Arrays.copyOf(score1, score1.length);
		for(int score : score2)	{
			System.out.printf("%3d", score);
		}
		System.out.println();
		score2[0] = 99;
		for(int score : score1)	{
			System.out.printf("%3d",  score);
		}
		System.out.println();
		int[] score3 = Arrays.copyOf(score1, score1.length * 2);
		for(int score : score3)	{
			System.out.printf("%3d", score);
		}
	}

}
