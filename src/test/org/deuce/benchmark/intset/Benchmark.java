package org.deuce.benchmark.intset;

import java.util.Random;

import org.deuce.Atomic;
import org.deuce.distribution.replication.Bootstrap;

/**
 * @author Pascal Felber
 * @since 0.1
 */
public class Benchmark implements org.deuce.benchmark.Benchmark
{
	@Bootstrap(id = 3)
	public IntSet m_set;
	int m_range = 1 << 16;
	int m_rate = 20;

	public void init(String[] args)
	{
		boolean error = false;
		int initial = 256;

		if (args.length == 0)
			error = true;
		for (int i = 1; i < args.length && !error; i++)
		{
			if (args[i].equals("-i"))
			{
				if (++i < args.length)
					initial = Integer.parseInt(args[i]);
				else
					error = true;
			}
			else if (args[i].equals("-r"))
			{
				if (++i < args.length)
					m_range = Integer.parseInt(args[i]);
				else
					error = true;
			}
			else if (args[i].equals("-w"))
			{
				if (++i < args.length)
					m_rate = Integer.parseInt(args[i]);
				else
					error = true;
			}
			else
				error = true;
		}
		if (Integer.getInteger("tribu.site") == 1)
		{
			error = initSet(args[0]);
			// else
			// waitForSet();

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
			System.err.println("Set populated: " + (end - start) / 1000000
					+ " ms");

			// for (int i = 0; i < initial; i++)
			// m_set.add(random.nextInt(m_range));

			System.out.println("Initial size        = " + initial);
			System.out.println("Range               = " + m_range);
			System.out.println("Write rate          = " + m_rate + "%");
			System.out.println();
		}
	}

	// @Atomic
	// private void waitForSet() {
	// if (m_set == null)
	// throw new TransactionException("Waiting for set");
	// }

	@Atomic
	private boolean initSet(String type)
	{
		if (type.equals("LinkedList"))
		{
			if (m_set == null)
			{
				m_set = new IntSetLinkedList();
				// initializeSet(random, initial);
			}
		}
		else if (type.equals("RBTree"))
		{
			if (m_set == null)
			{
				m_set = new RBTree();
				// initializeSet(random, initial);
			}
		}
		else if (type.equals("RedBTree"))
		{ // XXX supposed good red black tree
			if (m_set == null)
			{
				m_set = new RedBTree();
			}
		}
		else if (type.equals("RedBTreeX"))
		{
			if (m_set == null)
			{
				m_set = new RedBTreeX();
			}
		}
		else if (type.equals("SkipList"))
		{
			if (m_set == null)
			{
				m_set = new IntSetSkipList();
				// initializeSet(random, initial);
			}
		}
		else if (type.equals("IntSetHash"))
		{
			if (m_set == null)
			{
				m_set = new IntSetHash();
				// initializeSet(random, initial);
			}
		}
		else if (type.equals("IntJavaHashSet"))
		{
			if (m_set == null)
			{
				m_set = new IntJavaHashSet();
				// initializeSet(random, initial);
			}
		}
		else if (type.equals("IntJavaConcurrentHashSet"))
		{
			if (m_set == null)
			{
				m_set = new IntJavaConcurrentHashSet();
				// initializeSet(random, initial);
			}
		}
		else
		{
			return true;
		}

		return false;
	}

	// @Atomic
	// public void initializeSet(Random random, int initial) {
	// // for (int i = 0; i < initial; i++) {
	// // m_set.add(random.nextInt(m_range));
	// // }
	// int i = 0;
	// while (i < initial) {
	// if (m_set.add(random.nextInt(m_range)))
	// i++;
	// }
	// }

	public void initializeSet(Random random, int initial)
	{
		int chunkSize = initial / 8;
		for (int i = 0; i < initial; i += chunkSize)
		{
			System.err.println(i);
			addToSet(random, chunkSize);
		}
	}

	@Atomic
	public void addToSet(Random random, int chunkSize)
	{
		for (int i = 0; i < chunkSize; i++)
		{
			//System.err.println(i);
			while (!m_set.add(random.nextInt(m_range)))
				;
		}

	}

	public org.deuce.benchmark.BenchmarkThread createThread(int i, int nb)
	{
		return new BenchmarkThread(m_set, m_range, m_rate);
	}

	public String getStats(org.deuce.benchmark.BenchmarkThread[] threads)
	{
		int add = 0;
		int remove = 0;
		int contains = 0;
		for (int i = 0; i < threads.length; i++)
		{
			add += ((BenchmarkThread) threads[i]).m_nb_add;
			remove += ((BenchmarkThread) threads[i]).m_nb_remove;
			contains += ((BenchmarkThread) threads[i]).m_nb_contains;
		}
		return "A=" + add + ", R=" + remove + ", C=" + contains;
	}
}
