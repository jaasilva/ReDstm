package test.partial;

import org.deuce.Atomic;
import org.deuce.benchmark.Barrier;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.Bootstrap;

public class Main
{
	@Bootstrap(id = 1)
	static PartialList<Integer> intset;

	@Bootstrap(id = 2)
	static public Barrier start;

	@Bootstrap(id = 3)
	static public Barrier end;

	@Bootstrap(id = 4)
	static public Barrier close;

	public static void main(String[] args)
	{
		System.out.println("...");

		if (Integer.getInteger("tribu.site") == 1)
		{
			initSet();
			System.out.println("-- List initialized.");
		}

		int replicas = Integer.getInteger("tribu.replicas");
		initBarriers(replicas);
		System.out.println("-- Barriers initialized.");

		System.out.println("-- Starting...");
		start.join();

		add(Integer.getInteger("tribu.site"));
		add(Integer.getInteger("tribu.site") * 3);

		System.out.println("-- Ending...");
		end.join();

		if (Integer.getInteger("tribu.site") == 1)
		{
			printList(intset);
		}

		System.out.println("-- Closing...");
		close.join();

		TribuDSTM.close();
	}

	@Atomic
	private static void initSet()
	{
		if (intset == null)
		{
			intset = new PartialList<Integer>();
			intset.init();
		}
	}

	@Atomic
	private static void initBarriers(int replicas)
	{
		if (start == null)
			start = new Barrier(replicas);
		if (end == null)
			end = new Barrier(replicas);
		if (close == null)
			close = new Barrier(replicas);
	}

	@Atomic
	private static void add(int val)
	{
		intset.add(val);
	}

	@Atomic
	private static void printList(PartialList<Integer> list)
	{
		_PartialNode<Integer> n = list.head;

		while (n != null)
		{
			System.out.println(n.getValue());
			n = n.getNext();
		}
	}
}
