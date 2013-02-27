package jstamp.genome;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.deuce.transform.ExcludeTM;

public class Barrier
{

	private static CyclicBarrier barrier;

	public static void enterBarrier()
	{
		try
		{
			barrier.await();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		catch (BrokenBarrierException e)
		{
			e.printStackTrace();
		}
	}

	public static void setBarrier(int x)
	{
		barrier = new CyclicBarrier(x);
	}

}
