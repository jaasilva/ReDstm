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

	int m_range = 1 << 16;
	int m_rate = 10;
	int m_remote_read = 25;
	int initial = 32768; // 2^15;
	RedBTree local;
	RedBTree[] remote;

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
				}
				else
					error = true;
			}
			else
			{
				error = true;
			}
		}

		boolean isMaster = TribuDSTM.isGroupMaster();
		int numGroups = TribuDSTM.getNumGroups();
		Group group = TribuDSTM.getLocalGroup();

		if (isMaster)
		{
			System.err.println("############# MASTER");
			error = initCollection();

			if (error)
			{
				System.err.println("LOL WRONG ARGS!!!");
				System.exit(1);
			}

			Random rand = new Random();

			System.err.println("Collections created.");
			long start = System.nanoTime();
			if (numGroups == 1)
			{ // 2 Nodes
				System.out.println("rbt1");
				initializeSet(rbt1, rand, initial);
			}
			else if (numGroups == 2)
			{ // 4 Nodes
				if (group.getId() == 0)
				{
					System.out.println("rbt1");
					initializeSet(rbt1, rand, initial);
				}
				else
				{
					System.out.println("rbt2");
					initializeSet(rbt2, rand, initial);
				}
			}
			else if (numGroups == 3)
			{ // 6 Nodes
				if (group.getId() == 0)
				{
					System.out.println("rbt1");
					initializeSet(rbt1, rand, initial);
				}
				else if (group.getId() == 1)
				{
					System.out.println("rbt2");
					initializeSet(rbt2, rand, initial);
				}
				else
				{
					System.out.println("rbt3");
					initializeSet(rbt3, rand, initial);
				}
			}
			else if (numGroups == 4)
			{ // 8 Nodes
				if (group.getId() == 0)
				{
					System.out.println("rbt1");
					initializeSet(rbt1, rand, initial);
				}
				else if (group.getId() == 1)
				{
					System.out.println("rbt2");
					initializeSet(rbt2, rand, initial);
				}
				else if (group.getId() == 2)
				{
					System.out.println("rbt3");
					initializeSet(rbt3, rand, initial);
				}
				else
				{
					System.out.println("rbt4");
					initializeSet(rbt4, rand, initial);
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

		setCollections(numGroups, group.getId());
	}

	@Atomic
	private void setCollections(int numGroups, int groupId)
	{
		if (numGroups == 1)
		{
			m_remote_read = 0;

			local = rbt1;

			remote = new RedBTree[0];
		}
		else if (numGroups == 2)
		{
			if (groupId == 0)
			{
				local = rbt1;

				remote = new RedBTree[1];
				remote[0] = rbt2;
			}
			else
			{
				local = rbt2;

				remote = new RedBTree[1];
				remote[0] = rbt1;
			}
		}
		else if (numGroups == 3)
		{
			if (groupId == 0)
			{
				local = rbt1;

				remote = new RedBTree[2];
				remote[0] = rbt2;
				remote[1] = rbt3;
			}
			else if (groupId == 1)
			{
				local = rbt2;

				remote = new RedBTree[2];
				remote[0] = rbt1;
				remote[1] = rbt3;
			}
			else
			{
				local = rbt3;

				remote = new RedBTree[2];
				remote[0] = rbt1;
				remote[1] = rbt2;
			}
		}
		else if (numGroups == 4)
		{
			if (groupId == 0)
			{
				local = rbt1;

				remote = new RedBTree[3];
				remote[0] = rbt2;
				remote[1] = rbt3;
				remote[2] = rbt4;
			}
			else if (groupId == 1)
			{
				local = rbt2;

				remote = new RedBTree[3];
				remote[0] = rbt1;
				remote[1] = rbt3;
				remote[2] = rbt4;
			}
			else if (groupId == 2)
			{
				local = rbt3;

				remote = new RedBTree[3];
				remote[0] = rbt1;
				remote[1] = rbt2;
				remote[2] = rbt4;
			}
			else if (groupId == 3)
			{
				local = rbt4;

				remote = new RedBTree[3];
				remote[0] = rbt1;
				remote[1] = rbt2;
				remote[2] = rbt3;
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
