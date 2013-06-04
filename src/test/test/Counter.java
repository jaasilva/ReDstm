package test;

import org.deuce.Atomic;

public class Counter
{
	int x = 0;

	@Atomic
	public void inc()
	{
		x = x + 1;
	}

	@Atomic
	public int get()
	{
		return x;
	}
}
