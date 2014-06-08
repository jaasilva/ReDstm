package org.deuce.benchmark;

import org.deuce.Atomic;

public class Barrier
{
	public static boolean quiet = false;
	private volatile int counter;
	private volatile int expected;
	private static final long pollingPeriod = 500;

	public Barrier(int expected)
	{
		this.counter = 0;
		this.expected = expected;
	}

	public void join()
	{
		increment();

		if (!quiet)
		{
			System.err.println("Barrier increased to " + counter
					+ " (expected=" + expected + ")");
		}

		boolean exit = false;
		while (!exit)
		{
			try
			{
				if (counter >= expected)
				{
					exit = true;
				}

				Thread.sleep(pollingPeriod);
			}
			catch (InterruptedException e)
			{
				System.err.println("Who should be interrupting me?");
				e.printStackTrace();
			}
		}

		if (!quiet)
		{
			System.err.println("- Barrier exited!");
		}

		counter = 0;
	}

	@Atomic
	private void increment()
	{
		counter = counter + 1;
	}
}
