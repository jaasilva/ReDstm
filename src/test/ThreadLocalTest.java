import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLocalTest
{

	public static class Dummy
	{
		AtomicInteger i = new AtomicInteger(0);
		String s = new String();
		AtomicInteger j = new AtomicInteger(0);
	}

	public static class Pool
	{
		public Dummy[] arr = new Dummy[1 << 14];
		public int next = 0;

		public Pool()
		{
			for (int i = 0; i < arr.length; i++)
			{
				arr[i] = new Dummy();
			}
		}

		public Dummy getNext()
		{
			Dummy o = arr[next];
			next = (next + 1) % arr.length;
			return o;
		}
	}

	public static class Worker implements Runnable
	{
		public volatile boolean stop = false;
		public volatile boolean warm = true;

		public long count = 0;
		ThreadLocal<Pool> local = new ThreadLocal<Pool>()
		{
			@Override
			public Pool initialValue()
			{
				return new Pool();
			}
		};

		public void run()
		{
			local.get();
			while (warm)
			{

			}
			while (!stop)
			{
				Dummy o = local.get().getNext();
				count++;
			}
		}
	}

	public static void main(String[] args) throws InterruptedException
	{
		Worker[] ws = new Worker[2];
		Thread[] ts = new Thread[2];
		for (int i = 0; i < ws.length; i++)
		{
			ws[i] = new Worker();
			ts[i] = new Thread(ws[i]);
		}

		for (int i = 0; i < ws.length; i++)
		{
			ts[i].start();
		}

		Thread.sleep(1000);

		for (int i = 0; i < ws.length; i++)
		{
			ws[i].warm = false;
		}

		Thread.sleep(10000);

		for (int i = 0; i < ws.length; i++)
		{
			ws[i].stop = true;
		}

		for (int i = 0; i < ws.length; i++)
		{
			ts[i].join();
		}

		for (int i = 0; i < ws.length; i++)
		{
			System.out.println("T" + i + " -> " + ws[i].count);
		}
	}

}
