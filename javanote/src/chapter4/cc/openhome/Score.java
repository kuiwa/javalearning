package cc.openhome;

public class Score {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int[] scores = {88, 82, 86, 84, 59, 57, 15, 18};
		for(int i = 0; i < scores.length; i++)	{
			System.out.printf("%d%n", scores[i]);
		}
		for(int score: scores)	{
			System.out.printf("%d--", score);
		}
	}

}
