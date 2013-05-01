package trxsys.array;

import java.lang.reflect.Array;

public class GetLength
{
	public static void main(String[] args)
	{
		int[][] test = new int[2][2];
		Object obj = (Object) test;
		extracted(obj);
	}

	private static void extracted(Object obj)
	{
		Array.getLength(obj);
	}
}
