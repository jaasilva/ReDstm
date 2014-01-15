package test.partial;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;


import org.deuce.distribution.cache.CacheContainer;

/**
 * @author jaasilva
 */
public class DummyMain
{
	private static final Semaphore syncMsg = new Semaphore(0);
	private static final Executor exe = Executors.newFixedThreadPool(3);
	
	public static void main(String[] args)
	{
//		one a = new DummyMain.one();
//		two b = new DummyMain.two();
//
//		System.out.println("one");
//		exe.execute(a);
//		try
//		{
//			Thread.sleep(500);
//		}
//		catch (InterruptedException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		System.out.println("two");
//		exe.execute(b);
//		System.out.println("end");
		
		
//		TreeSet<CacheContainer> t = new TreeSet<CacheContainer>();
//		
//		CacheContainer a = new CacheContainer();
//		a.version = 1;
//		CacheContainer b = new CacheContainer();
//		b.version = 7;
//		CacheContainer c = new CacheContainer();
//		c.version = 3;
//		
//		t.add(b);
//		t.add(a);
//		t.add(c);
//		
//		
//		Iterator<CacheContainer> it = t.iterator();
//		while(it.hasNext())
//		{
//			System.out.println(it.next().version);
//		}
		
		
		
		SortedSet<CacheContainer> s = new TreeSet<CacheContainer>();
		
		CacheContainer a = new CacheContainer();
		a.version = 1;
		CacheContainer b = new CacheContainer();
		b.version = 7;
		CacheContainer c = new CacheContainer();
		c.version = 3;
		
		s.add(a);
		s.add(b);
		s.add(c);
		
		System.out.println(">>> " + s.first().version);
		
		Iterator<CacheContainer> it = s.iterator();
		while(it.hasNext())
		{
			System.out.println(it.next().version);
		}
		
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
