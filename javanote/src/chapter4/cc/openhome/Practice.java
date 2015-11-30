package cc.openhome;

public class Practice {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int x = 300;
		int y = 300;
		Integer wx = x;
		Integer wy = y;
		System.out.println(x == y);
		System.out.println(wx == wy);
		System.out.println(x == wx);
		System.out.println(y == wy);
		System.out.println(wx.equals(x));
		System.out.println(wy.equals(y));
		
		int[] arr1 = {1, 2, 3};
		int[] arr2 = arr1;
		arr2[1] = 20;
		System.out.println(arr1[1]);
		
		int[] array1 = {1, 2, 3};
		int[] array2 = new int[arr1.length];
		array2 = array1;
		for(int value: array2)	{
			System.out.printf("%d", value);
		}
		
		String[] str = new String[5];
		for(int value = 0; value < str.length; value++)	{		
			System.out.println(str[value]);
		}
		
		String[] strs = {"Java","Java","Java","Java","Java"};
		for(int value = 0; value < strs.length; value++)	{		
			System.out.println(strs[value]);
		}
		
		String[][] str_arr = {
				{"java","java","java"},
				{"java","java","java","java"}
		};
		for(String[] row: str_arr)	{
			for(String value: row)	{
				System.out.println(value);
			}
		}
		
	}

}
