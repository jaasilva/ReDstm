package org.deuce.benchmark.intset;

import java.util.Random;

import org.deuce.Atomic;
import org.deuce.distribution.Defaults;
import org.deuce.distribution.replication.Bootstrap;

/**
 * @author Pascal Felber
 * @since 0.1
 */
public class Benchmark2 implements org.deuce.benchmark.Benchmark
{
	@Bootstrap(id = 3)
	public IntSet m_set;
	int m_range = 1 << 16;
	int m_rate = 20;
	int initial = 32768; // 2^15

	@Atomic
	private void setRange(int x)
	{
		m_range = x;
	}

	@Atomic
	private void setWrites(int x)
	{
		m_rate = x;
	}

	@Atomic
	private void setInitial(int x)
	{
		initial = x;
	}

	@Override
	public void init(String[] args)
	{
		boolean error = false;

		if (args.length == 0)
			error = true;
		for (int i = 1; i < args.length && !error; i++)
		{
			if (args[i].equals("-i"))
			{
				if (++i < args.length)
				{
					initial = Integer.parseInt(args[i]);
					setInitial(initial);
					System.setProperty(Defaults._RBTREE_INITIAL, "" + initial);
				}
				else
					error = true;
			}
			else if (args[i].equals("-r"))
			{
				if (++i < args.length)
				{
					m_range = Integer.parseInt(args[i]);
					setRange(m_range);
				}
				else
					error = true;
			}
			else if (args[i].equals("-w"))
			{
				if (++i < args.length)
				{
					m_rate = Integer.parseInt(args[i]);
					setWrites(m_rate);
				}
				else
					error = true;
			}
			else if (args[i].equals("-po"))
			{
				if (++i < args.length)
				{
					int partial_ops = Integer.parseInt(args[i]);
					System.setProperty(Defaults._RBTREE_PARTIAL_OPS, ""
							+ partial_ops);
				}
				else
					error = true;
			}
			else
				error = true;
		}
		if (Integer.getInteger("tribu.site") == 1)
		{
			error = initSet(args[0]);

			if (error)
			{
				System.out
						.println("Benchmark arguments: (LinkedList|SkipList|RBTree) [-i initial-size] [-r range] [-w write-rate]");
				System.exit(1);
			}

			System.err.println("Set created.");

			long start = System.nanoTime();
			initializeSet(new Random(), initial);
			long end = System.nanoTime();
			System.err.println("Set populated: " + (end - start) / 1000000000.0
					+ " s");

			System.out.println("Initial size  = " + initial);
			System.out.println("Range         = " + m_range);
			System.out.println("Write rate    = " + m_rate + "%");
			System.out.println();
		}
	}

	@Atomic
	private boolean initSet(String type)
	{
		if (type.equals("LinkedList"))
		{
			if (m_set == null)
			{
				m_set = new IntSetLinkedList();
			}
		}
		else if (type.equals("RedBTree"))
		{ // supposed good red black tree
			if (m_set == null)
			{
				m_set = new RedBTree();
			}
		}
		else if (type.equals("RedBTreeX"))
		{ // memory benchmark
			if (m_set == null)
			{
				m_set = new RedBTreeX();
			}
		}
		else if (type.equals("RedBTreeZ"))
		{ // partial replication benchmark
			if (m_set == null)
			{
				m_set = new RedBTreeZ();
			}
		}
		else if (type.equals("SkipList"))
		{
			if (m_set == null)
			{
				m_set = new IntSetSkipList();
			}
		}
		else
		{
			return true;
		}
		return false;
	}

	public void initializeSet(Random random, int initial)
	{
		int chunkSize = initial / 8;
		for (int i = 0; i < initial; i += chunkSize)
		{
			System.err.println("[" + i + "-" + (i + chunkSize) + "]");
			addToSet(random, chunkSize);
		}
	}

	@Atomic
	public void addToSet(Random random, int chunkSize)
	{
		for (int i = 0; i < chunkSize; i++)
		{
			while (!m_set.initAdd(random.nextInt(m_range)))
				;
		}
	}

	@Override
	public org.deuce.benchmark.BenchmarkThread createThread(int i, int nb)
	{
		return new BenchmarkThread2(m_set, m_range, m_rate);
	}

	@Override
	public String getStats(org.deuce.benchmark.BenchmarkThread[] threads)
	{
		int add = 0;
		int remove = 0;
		int contains = 0;
		for (int i = 0; i < threads.length; i++)
		{
			add += ((BenchmarkThread2) threads[i]).m_nb_add;
			remove += ((BenchmarkThread2) threads[i]).m_nb_remove;
			contains += ((BenchmarkThread2) threads[i]).m_nb_contains;
		}
		return "A=" + add + ", R=" + remove + ", C=" + contains;
	}

	@Override
	public boolean validate()
	{
		return m_set.validate();
	}
}
