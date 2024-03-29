package jstamp.vacation;

import java.util.concurrent.CyclicBarrier;

public class Barrier
{
	private static CyclicBarrier barrier;

	public static void enterBarrier()
	{
		try
		{
			barrier.await();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public static void setBarrier(int x)
	{
		barrier = new CyclicBarrier(x);
	}

	public static void assertIsClear()
	{
		int numberWaiting = barrier.getNumberWaiting();
		if (numberWaiting != 0)
		{
			System.out.println(String.format("Bad barrier: %d waiting",
					numberWaiting));
		}
		else
		{
			System.out.println("Barrier is clear.");
		}
	}
}
