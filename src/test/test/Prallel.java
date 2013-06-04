package test;

import java.util.Random;

import org.deuce.Atomic;
import org.deuce.benchmark.Barrier;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.Bootstrap;

public class Prallel
{
	@Bootstrap(id = 1)
	static public Counter count;

	@Bootstrap(id = 2)
	static public Barrier start;

	@Bootstrap(id = 3)
	static public Barrier end;

	public static void main(String[] args) throws Exception
	{
		System.out.println("...");

		if (Integer.getInteger("tribu.site") == 1)
		{
			init();
			System.out.println("-- Counter initialized.");
		}

		int replicas = Integer.getInteger("tribu.replicas");
		initBarriers(replicas);
		System.out.println("-- Barriers initialized.");

		Barrier s = getStartBarrier();
		System.out.println("-- Starting...");
		s.join();

		System.out.println(">>> " + get());

		Random r = new Random();
		for (int i = 0; i < 10; i++)
		{
			inc();
			System.out.println(get());

			Thread.sleep(r.nextInt(1000));
		}

		Barrier e = getEndBarrier();
		System.out.println("-- Ending...");
		e.join();

		System.out.println(">>> " + get());

		TribuDSTM.close();
	}

	@Atomic
	private static void init()
	{
		if (count == null)
		{
			count = new Counter();
		}
	}

	@Atomic
	private static void inc()
	{
		count.inc();
	}

	@Atomic
	private static int get()
	{
		return count.get();
	}

	@Atomic
	private static Barrier getStartBarrier()
	{
		return start;
	}

	@Atomic
	private static Barrier getEndBarrier()
	{
		return end;
	}

	@Atomic
	private static void initBarriers(int replicas)
	{
		if (start == null)
			start = new Barrier(replicas);
		if (end == null)
			end = new Barrier(replicas);
	}
}
