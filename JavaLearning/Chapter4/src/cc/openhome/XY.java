package cc.openhome;

public class XY {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int[][] cords = {
				{1, 2, 3},
				{4, 5, 6}
		};
		for(int i = 0; i < cords.length; i++)	{
			for(int j = 0; j < cords[i].length; j++)	{
				System.out.printf("%2d", cords[i][j]);
			}
		System.out.println();
		}
		for(int[] row: cords)	{
			for(int value: row)	{
				System.out.printf("%2d", value);
			}
		System.out.println();
		}
	}

}
