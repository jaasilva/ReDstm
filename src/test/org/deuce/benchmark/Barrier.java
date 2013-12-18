package org.deuce.benchmark;

import org.deuce.Atomic;

public class Barrier
{
	protected volatile int counter;
	protected volatile int expected;
	private static final long pollingPeriod = 500;

	public Barrier(int expected)
	{
		counter = 0;
		this.expected = expected;
	}

	public void join()
	{
		increment();

		System.err.println("Barrier increased to " + counter + " (expected="
				+ expected + ")");
		boolean exit = false;
		while (!exit)
		{
			try
			{
				int c = counter;
				boolean res = (c >= expected);
				if (res)
				{
					exit = true;
				}

				Thread.sleep(pollingPeriod);
			}
			catch (InterruptedException e)
			{
				System.err.println("Pao -> who should be interrupting me?");
				e.printStackTrace();
			}
		}

		System.err.println("-- Barrier increased to " + counter + " (expected="
				+ expected + ")");
		counter = 0;
	}

	@Atomic
	private void increment()
	{
		counter = counter + 1;
	}
}
