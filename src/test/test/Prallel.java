package test;

import org.deuce.distribution.replication.Bootstrap;
import org.deuce.Atomic;

public class Prallel
{
	@Bootstrap(id = 1)
	static public Counter c;
	
	/**
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception
	{
		
		init();

//		Thread[] threads = new Thread[1];
//		for (int i = 0; i < threads.length; ++i)
//		{
//			threads[i] = new Thread()
//			{
//				@Override
//				public void run()
//				{
					System.out.println(".");
					for (int z = 0; z < 50; ++z)
					{
//						try
//						{
							c.inc();

//						}
//						catch (Exception e)
//						{
//							System.err.println("e");
//							--z;
//						}
						System.out.println(c.get());
					}
//					System.out.println(c.get());
//				}
//			};
//			threads[i].start();
//		}
//
//		for (int i = 0; i < threads.length; ++i)
//		{
//			threads[i].join();
//		}

		// System.out.println( c.get());
		// System.out.println( c.get());

	}
	
	@Atomic
	public static void init()
	{
		if (c == null)
			c = new Counter();
	}
}
