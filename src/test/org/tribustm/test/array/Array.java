package org.tribustm.test.array;

public class Array
{
	int a;
	String b;
	int[] c = new int[2];
	String[] d = new String[2];
	long[][] e = new long[2][2];
	Object[][] f = new Object[2][];

	public int arrayIntGet()
	{
		return c[1];
	}

	public void arrayIntSet(int v)
	{
		c[1] = v;
	}

	public String arrayObjectGet()
	{
		return d[1];
	}

	public void arrayObjectSet(String s)
	{
		d[0] = s;
	}

	public long[] matrixIntGet()
	{
		return e[0];
	}

	public void matrixIntSet(long[] m, long v)
	{
		e[0] = m;
		e[1][0] = v;
	}

	public Object[] matrixObjectGet()
	{
		return f[0];
	}

	public void matrixObjectSet(Object[] m, Object v)
	{
		f[0] = null;
		f[1][0] = v;
	}

	public static void main(String[] args)
	{

	}
}
