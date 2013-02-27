package org.tribustm.test.array;

import org.deuce.Atomic;

public class ObjectPointer
{
	Object f;
	Object[] g;

	void test()
	{
		f = new int[2];
		((int[]) f)[0] = 1;
		f = "aaa";
	}

	void test2()
	{
		g = new Object[2];
		g[0] = "bbb";
		g[1] = new int[2];
		((int[]) g[1])[0] = 1;
	}

	@Atomic
	void atomic()
	{
		test();
	}

	@Atomic
	void atomic2()
	{
		test2();
	}

	public static void main(String[] args)
	{
		ObjectPointer op = new ObjectPointer();
		op.test();
		op.atomic();
		op.test2();
		op.atomic2();
	}
}
