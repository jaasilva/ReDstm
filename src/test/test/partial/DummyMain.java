package test.partial;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author jaasilva
 */
public class DummyMain
{
	private static final Semaphore syncMsg = new Semaphore(0);
	private static final Executor exe = Executors.newFixedThreadPool(3);

	public static void main(String[] args)
	{
		one a = new DummyMain.one();
		two b = new DummyMain.two();

		System.out.println("one");
		exe.execute(a);
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("two");
		exe.execute(b);
		System.out.println("end");
	}

	static class one implements Runnable
	{
		@Override
		public void run()
		{
			System.out.println(">> " + syncMsg.availablePermits());
			try
			{
				syncMsg.acquire();
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(">> " + syncMsg.availablePermits());
		}
	}

	static class two implements Runnable
	{
		@Override
		public void run()
		{
			System.out.println("<< " + syncMsg.availablePermits());
			syncMsg.release();
			System.out.println("<< " + syncMsg.availablePermits());
		}
	}
}
