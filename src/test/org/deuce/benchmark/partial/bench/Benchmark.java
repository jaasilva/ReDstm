package org.deuce.benchmark.partial.bench;

import java.util.Random;

import org.deuce.Atomic;
import org.deuce.distribution.Defaults;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.Bootstrap;
import org.deuce.distribution.replication.group.Group;

/**
 * @author jaasilva
 */
public class Benchmark implements org.deuce.benchmark.Benchmark
{
	@Bootstrap(id = 8080)
	public RedBTree rbt1;
	@Bootstrap(id = 8081)
	public RedBTree rbt2;
	@Bootstrap(id = 8082)
	public RedBTree rbt3;
	@Bootstrap(id = 8083)
	public RedBTree rbt4;
	@Bootstrap(id = 8084)
	public RedBTree rbt5;
	@Bootstrap(id = 8085)
	public RedBTree rbt6;
	@Bootstrap(id = 8086)
	public RedBTree rbt7;
	@Bootstrap(id = 8087)
	public RedBTree rbt8;
	@Bootstrap(id = 8088)
	public RedBTree rbt9;

	int m_range = 1 << 16;
	int m_rate = 10;
	int m_remote_read = 25;
	int initial = 32768; // 2^15;
	RedBTree[] local, remote;

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
		for (int i = 0; i < args.length && !error; i++)
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
			else if (args[i].equals("-rr"))
			{
				if (++i < args.length)
				{
					m_remote_read = Integer.parseInt(args[i]);
					System.setProperty(Defaults._RBTREE_RR, "" + m_remote_read);
				}
				else
					error = true;
			}
			else
			{
				error = true;
			}
		}

		Address id = TribuDSTM.getLocalAddress();
		boolean isMaster = TribuDSTM.isGroupMaster();
		int numGroups = TribuDSTM.getNumGroups();
		Group group = TribuDSTM.getLocalGroup();

		System.out.println(">>> Local addr: " + id);
		System.out.println(">>> Is Master: " + isMaster);

		if (isMaster)
		{
			System.err.println("############# MASTER");
			error = initCollection();

			if (error)
			{
				System.out.println("LOL WRONG ARGS!!!");
				System.exit(1);
			}

			Random rand = new Random();

			System.err.println("Collections created.");
			long start = System.nanoTime();
			if (numGroups == 1)
			{ // 2 Nodes
				System.out.println("rbt1");
				initializeSet(rbt1, rand, initial);
				System.out.println("rbt2");
				initializeSet(rbt2, rand, initial);
				System.out.println("rbt3");
				initializeSet(rbt3, rand, initial);
				System.out.println("rbt4");
				initializeSet(rbt4, rand, initial);
				System.out.println("rbt5");
				initializeSet(rbt5, rand, initial);
				System.out.println("rbt6");
				initializeSet(rbt6, rand, initial);
				System.out.println("rbt7");
				initializeSet(rbt7, rand, initial);
				System.out.println("rbt8");
				initializeSet(rbt8, rand, initial);
			}
			else if (numGroups == 2)
			{ // 4 Nodes
				if (group.getId() == 0)
				{
					System.out.println("rbt1");
					initializeSet(rbt1, rand, initial);
					System.out.println("rbt2");
					initializeSet(rbt2, rand, initial);
					System.out.println("rbt3");
					initializeSet(rbt3, rand, initial);
					System.out.println("rbt4");
					initializeSet(rbt4, rand, initial);
				}
				else
				{
					System.out.println("rbt5");
					initializeSet(rbt5, rand, initial);
					System.out.println("rbt6");
					initializeSet(rbt6, rand, initial);
					System.out.println("rbt7");
					initializeSet(rbt7, rand, initial);
					System.out.println("rbt8");
					initializeSet(rbt8, rand, initial);
				}
			}
			else if (numGroups == 3)
			{ // 6 Nodes
				if (group.getId() == 0)
				{
					System.out.println("rbt1");
					initializeSet(rbt1, rand, initial);
					System.out.println("rbt2");
					initializeSet(rbt2, rand, initial);
					System.out.println("rbt3");
					initializeSet(rbt3, rand, initial);
				}
				else if (group.getId() == 1)
				{
					System.out.println("rbt4");
					initializeSet(rbt4, rand, initial);
					System.out.println("rbt5");
					initializeSet(rbt5, rand, initial);
					System.out.println("rbt6");
					initializeSet(rbt6, rand, initial);
				}
				else
				{
					System.out.println("rbt7");
					initializeSet(rbt7, rand, initial);
					System.out.println("rbt8");
					initializeSet(rbt8, rand, initial);
					System.out.println("rbt9");
					initializeSet(rbt9, rand, initial);
				}
			}
			else if (numGroups == 4)
			{ // 8 Nodes
				if (group.getId() == 0)
				{
					System.out.println("rbt1");
					initializeSet(rbt1, rand, initial);
					System.out.println("rbt2");
					initializeSet(rbt2, rand, initial);
				}
				else if (group.getId() == 1)
				{
					System.out.println("rbt3");
					initializeSet(rbt3, rand, initial);
					System.out.println("rbt4");
					initializeSet(rbt4, rand, initial);
				}
				else if (group.getId() == 2)
				{
					System.out.println("rbt5");
					initializeSet(rbt5, rand, initial);
					System.out.println("rbt6");
					initializeSet(rbt6, rand, initial);
				}
				else
				{
					System.out.println("rbt7");
					initializeSet(rbt7, rand, initial);
					System.out.println("rbt8");
					initializeSet(rbt8, rand, initial);
				}
			}
			long end = System.nanoTime();
			System.err.println("Set populated: " + (end - start) / 1000000
					+ " ms");

			System.out.println("Initial size        = " + initial);
			System.out.println("Key Range           = " + m_range);
			System.out.println("Write rate          = " + m_rate + "%");
			System.out.println("Remote Read rate    = " + m_remote_read + "%");
			System.out.println();
		}
		else
		{
			System.err.println("############# SLAVE");
			try
			{
				Thread.sleep(2000);
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
		}

		if (numGroups == 1)
		{
			System.setProperty(Defaults._RBTREE_RR, "" + 0);

			local = new RedBTree[8];
			local[0] = rbt1;
			local[1] = rbt2;
			local[2] = rbt3;
			local[3] = rbt4;
			local[4] = rbt5;
			local[5] = rbt6;
			local[6] = rbt7;
			local[7] = rbt8;

			remote = new RedBTree[0];
		}
		else if (numGroups == 2)
		{
			if (group.getId() == 0)
			{
				local = new RedBTree[4];
				local[0] = rbt1;
				local[1] = rbt2;
				local[2] = rbt3;
				local[3] = rbt4;

				remote = new RedBTree[4];
				remote[0] = rbt5;
				remote[1] = rbt6;
				remote[2] = rbt7;
				remote[3] = rbt8;
			}
			else
			{
				local = new RedBTree[4];
				local[0] = rbt5;
				local[1] = rbt6;
				local[2] = rbt7;
				local[3] = rbt8;

				remote = new RedBTree[4];
				remote[0] = rbt1;
				remote[1] = rbt2;
				remote[2] = rbt3;
				remote[3] = rbt4;
			}
		}
		else if (numGroups == 3)
		{
			if (group.getId() == 0)
			{
				local = new RedBTree[3];
				local[0] = rbt1;
				local[1] = rbt2;
				local[2] = rbt3;

				remote = new RedBTree[6];
				remote[0] = rbt4;
				remote[1] = rbt5;
				remote[2] = rbt6;
				remote[3] = rbt7;
				remote[4] = rbt8;
				remote[5] = rbt9;
			}
			else if (group.getId() == 1)
			{
				local = new RedBTree[3];
				local[0] = rbt4;
				local[1] = rbt5;
				local[2] = rbt6;

				remote = new RedBTree[6];
				remote[0] = rbt1;
				remote[1] = rbt2;
				remote[2] = rbt3;
				remote[3] = rbt7;
				remote[4] = rbt8;
				remote[5] = rbt9;
			}
			else
			{
				local = new RedBTree[3];
				local[0] = rbt7;
				local[1] = rbt8;
				local[2] = rbt9;

				remote = new RedBTree[6];
				remote[0] = rbt1;
				remote[1] = rbt2;
				remote[2] = rbt3;
				remote[3] = rbt4;
				remote[4] = rbt5;
				remote[5] = rbt6;
			}
		}
		else if (numGroups == 4)
		{
			if (group.getId() == 0)
			{
				local = new RedBTree[2];
				local[0] = rbt1;
				local[1] = rbt2;

				remote = new RedBTree[6];
				remote[0] = rbt3;
				remote[1] = rbt4;
				remote[2] = rbt5;
				remote[3] = rbt6;
				remote[4] = rbt7;
				remote[5] = rbt8;
			}
			else if (group.getId() == 1)
			{
				local = new RedBTree[2];
				local[0] = rbt3;
				local[1] = rbt4;

				remote = new RedBTree[6];
				remote[0] = rbt1;
				remote[1] = rbt2;
				remote[2] = rbt5;
				remote[3] = rbt6;
				remote[4] = rbt7;
				remote[5] = rbt8;
			}
			else if (group.getId() == 2)
			{
				local = new RedBTree[2];
				local[0] = rbt5;
				local[1] = rbt6;

				remote = new RedBTree[6];
				remote[0] = rbt1;
				remote[1] = rbt2;
				remote[2] = rbt3;
				remote[3] = rbt4;
				remote[4] = rbt7;
				remote[5] = rbt8;
			}
			else if (group.getId() == 3)
			{
				local = new RedBTree[2];
				local[0] = rbt7;
				local[1] = rbt8;

				remote = new RedBTree[6];
				remote[0] = rbt1;
				remote[1] = rbt2;
				remote[2] = rbt3;
				remote[3] = rbt4;
				remote[4] = rbt5;
				remote[5] = rbt6;
			}
		}
	}

	public void initializeSet(RedBTree rbt, Random random, int initial)
	{
		int chunkSize = initial / 8;
		for (int i = 0; i < initial; i += chunkSize)
		{
			System.err.println("[" + i + "-" + (i + chunkSize) + "]");
			addToSet(rbt, random, chunkSize);
		}
	}

	@Atomic
	public void addToSet(RedBTree rbt, Random random, int chunkSize)
	{
		for (int i = 0; i < chunkSize; i++)
		{
			while (!rbt.initAdd(random.nextInt(m_range)))
				;
		}
	}

	@Atomic
	private boolean initCollection()
	{
		if (rbt1 == null)
			rbt1 = new RedBTree();
		if (rbt2 == null)
			rbt2 = new RedBTree();
		if (rbt3 == null)
			rbt3 = new RedBTree();
		if (rbt4 == null)
			rbt4 = new RedBTree();
		if (rbt5 == null)
			rbt5 = new RedBTree();
		if (rbt6 == null)
			rbt6 = new RedBTree();
		if (rbt7 == null)
			rbt7 = new RedBTree();
		if (rbt8 == null)
			rbt8 = new RedBTree();
		if (rbt9 == null)
			rbt9 = new RedBTree();

		return false;
	}

	@Override
	public org.deuce.benchmark.BenchmarkThread createThread(int i, int nb)
	{
		return new org.deuce.benchmark.partial.bench.BenchmarkThread(local,
				remote, m_rate, m_range, m_remote_read);
	}

	@Override
	public String getStats(org.deuce.benchmark.BenchmarkThread[] threads)
	{
		int add = 0;
		int remove = 0;
		int contains_remote = 0;
		int contains_local = 0;
		for (int i = 0; i < threads.length; i++)
		{
			add += ((org.deuce.benchmark.partial.bench.BenchmarkThread) threads[i]).m_nb_add;
			remove += ((org.deuce.benchmark.partial.bench.BenchmarkThread) threads[i]).m_nb_remove;
			contains_remote += ((org.deuce.benchmark.partial.bench.BenchmarkThread) threads[i]).m_nb_contains_remote;
			contains_local += ((org.deuce.benchmark.partial.bench.BenchmarkThread) threads[i]).m_nb_contains_local;
		}
		return "A=" + add + ", R=" + remove + ", C(r)=" + contains_remote
				+ ", C(l)=" + contains_local;
	}

	@Override
	public boolean validate()
	{
		return false;
	}
}
