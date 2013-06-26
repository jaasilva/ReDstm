package org.deuce.benchmark.partial.photos;

import java.util.Random;

import org.deuce.Atomic;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.Bootstrap;
import org.deuce.distribution.replication.group.Group;

public class Benchmark implements org.deuce.benchmark.Benchmark
{
	@Bootstrap(id = 3)
	public RBTree photos_1;
	@Bootstrap(id = 4)
	public RBTree photos_2;
	@Bootstrap(id = 5)
	public RBTree photos_3;
	@Bootstrap(id = 6)
	public RBTree photos_4;
	@Bootstrap(id = 7)
	public RBTree photos_5;
	@Bootstrap(id = 8)
	public RBTree photos_6;

	int m_max_range, m_min_range, m_key_range;
	int m_rate = 10, m_read_rate = 10;
	RBTree[] myTrees;
	RBTree[] otherTrees;

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
			else if (args[i].equals("-rmax"))
			{
				if (++i < args.length)
					m_max_range = Integer.parseInt(args[i]);
				else
					error = true;
			}
			else if (args[i].equals("-r"))
			{
				if (++i < args.length)
					m_key_range = Integer.parseInt(args[i]);
				else
					error = true;
			}
			else if (args[i].equals("-rmin"))
			{
				if (++i < args.length)
					m_min_range = Integer.parseInt(args[i]);
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
			else if (args[i].equals("-rr"))
			{
				if (++i < args.length)
					m_read_rate = Integer.parseInt(args[i]);
				else
					error = true;
			}
			else
			{
				error = true;
			}
		}

		Address id = TribuDSTM.getLocalAddress();
		Group group = TribuDSTM.getLocalGroup();
		Address master = group.getAll().iterator().next();
		int numGroups = TribuDSTM.getNumGroups();

		System.out.println(">>> Local addr: " + id);
		System.out.println(">>> Master addr: " + master);
		System.out.println(">>> Group ID: " + group.getId());

		if (id.equals(master))
		{
			System.err.println("############# MASTER");
			error = initCollection();

			if (error)
			{
				System.out.println("LOL WRONG ARGS!!!");
				System.exit(1);
			}

			System.err.println("Collections created.");
			long start = System.nanoTime();
			if (numGroups == 1)
			{
				m_read_rate = 0;
				
				System.out.println("photos_1");
				initializeSet(photos_1, new Random(), initial);
				System.out.println("photos_2");
				initializeSet(photos_2, new Random(), initial);
				System.out.println("photos_3");
				initializeSet(photos_3, new Random(), initial);
				System.out.println("photos_4");
				initializeSet(photos_4, new Random(), initial);
				System.out.println("photos_5");
				initializeSet(photos_5, new Random(), initial);
				System.out.println("photos_6");
				initializeSet(photos_6, new Random(), initial);
			}
			else if (numGroups == 2)
			{
				if (group.getId() == 0)
				{
					System.out.println("photos_1");
					initializeSet(photos_1, new Random(), initial);
					System.out.println("photos_2");
					initializeSet(photos_2, new Random(), initial);
					System.out.println("photos_3");
					initializeSet(photos_3, new Random(), initial);
				}
				else
				{
					System.out.println("photos_4");
					initializeSet(photos_4, new Random(), initial);
					System.out.println("photos_5");
					initializeSet(photos_5, new Random(), initial);
					System.out.println("photos_6");
					initializeSet(photos_6, new Random(), initial);
				}
			}
			else if (numGroups == 3)
			{
				if (group.getId() == 0)
				{
					initializeSet(photos_1, new Random(), initial);
					initializeSet(photos_2, new Random(), initial);
				}
				else if (group.getId() == 1)
				{
					initializeSet(photos_3, new Random(), initial);
					initializeSet(photos_4, new Random(), initial);
				}
				else
				{
					initializeSet(photos_5, new Random(), initial);
					initializeSet(photos_6, new Random(), initial);
				}
			}
			else if (numGroups == 6)
			{
				if (group.getId() == 0)
				{
					initializeSet(photos_1, new Random(), initial);
				}
				else if (group.getId() == 1)
				{
					initializeSet(photos_2, new Random(), initial);
				}
				else if (group.getId() == 2)
				{
					initializeSet(photos_3, new Random(), initial);
				}
				else if (group.getId() == 3)
				{
					initializeSet(photos_4, new Random(), initial);
				}
				else if (group.getId() == 4)
				{
					initializeSet(photos_5, new Random(), initial);
				}
				else if (group.getId() == 5)
				{
					initializeSet(photos_6, new Random(), initial);
				}
			}
			long end = System.nanoTime();
			System.err.println("Set populated: " + (end - start) / 1000000
					+ " ms");

			System.out.println("Initial size        = " + initial);
			System.out.println("Image Size Range    = " + m_min_range + "-"
					+ m_max_range);
			System.out.println("Key Range           = " + m_key_range);
			System.out.println("Write rate          = " + m_rate + "%");
			System.out.println("Remote Read rate    = " + m_read_rate + "%");
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
			myTrees = new RBTree[6];
			myTrees[0] = photos_1;
			myTrees[1] = photos_2;
			myTrees[2] = photos_3;
			myTrees[3] = photos_4;
			myTrees[4] = photos_5;
			myTrees[5] = photos_6;

			otherTrees = new RBTree[0];
		}
		else if (numGroups == 2)
		{
			if (group.getId() == 0)
			{
				myTrees = new RBTree[3];
				myTrees[0] = photos_1;
				myTrees[1] = photos_2;
				myTrees[2] = photos_3;

				otherTrees = new RBTree[3];
				otherTrees[0] = photos_4;
				otherTrees[1] = photos_5;
				otherTrees[2] = photos_6;
			}
			else
			{
				myTrees = new RBTree[3];
				myTrees[0] = photos_4;
				myTrees[1] = photos_5;
				myTrees[2] = photos_6;

				otherTrees = new RBTree[3];
				otherTrees[0] = photos_1;
				otherTrees[1] = photos_2;
				otherTrees[2] = photos_3;
			}
		}
		else if (numGroups == 3)
		{
			if (group.getId() == 0)
			{
				myTrees = new RBTree[2];
				myTrees[0] = photos_1;
				myTrees[1] = photos_2;

				otherTrees = new RBTree[4];
				otherTrees[0] = photos_3;
				otherTrees[1] = photos_4;
				otherTrees[2] = photos_5;
				otherTrees[3] = photos_6;
			}
			else if (group.getId() == 1)
			{
				myTrees = new RBTree[2];
				myTrees[0] = photos_3;
				myTrees[1] = photos_4;

				otherTrees = new RBTree[4];
				otherTrees[0] = photos_1;
				otherTrees[1] = photos_2;
				otherTrees[2] = photos_5;
				otherTrees[3] = photos_6;
			}
			else
			{
				myTrees = new RBTree[2];
				myTrees[0] = photos_5;
				myTrees[1] = photos_6;

				otherTrees = new RBTree[4];
				otherTrees[0] = photos_1;
				otherTrees[1] = photos_2;
				otherTrees[2] = photos_3;
				otherTrees[3] = photos_4;
			}
		}
		else if (numGroups == 6)
		{
			if (group.getId() == 0)
			{
				myTrees = new RBTree[1];
				myTrees[0] = photos_1;

				otherTrees = new RBTree[5];
				otherTrees[0] = photos_2;
				otherTrees[1] = photos_3;
				otherTrees[2] = photos_4;
				otherTrees[3] = photos_5;
				otherTrees[4] = photos_6;
			}
			else if (group.getId() == 1)
			{
				myTrees = new RBTree[1];
				myTrees[0] = photos_2;

				otherTrees = new RBTree[5];
				otherTrees[0] = photos_1;
				otherTrees[1] = photos_3;
				otherTrees[2] = photos_4;
				otherTrees[3] = photos_5;
				otherTrees[4] = photos_6;
			}
			else if (group.getId() == 2)
			{
				myTrees = new RBTree[1];
				myTrees[0] = photos_3;

				otherTrees = new RBTree[5];
				otherTrees[0] = photos_2;
				otherTrees[1] = photos_1;
				otherTrees[2] = photos_4;
				otherTrees[3] = photos_5;
				otherTrees[4] = photos_6;
			}
			else if (group.getId() == 3)
			{
				myTrees = new RBTree[1];
				myTrees[0] = photos_4;

				otherTrees = new RBTree[5];
				otherTrees[0] = photos_2;
				otherTrees[1] = photos_3;
				otherTrees[2] = photos_1;
				otherTrees[3] = photos_5;
				otherTrees[4] = photos_6;
			}
			else if (group.getId() == 4)
			{
				myTrees = new RBTree[1];
				myTrees[0] = photos_5;

				otherTrees = new RBTree[5];
				otherTrees[0] = photos_2;
				otherTrees[1] = photos_3;
				otherTrees[2] = photos_4;
				otherTrees[3] = photos_1;
				otherTrees[4] = photos_6;
			}
			else if (group.getId() == 5)
			{
				myTrees = new RBTree[1];
				myTrees[0] = photos_6;

				otherTrees = new RBTree[5];
				otherTrees[0] = photos_2;
				otherTrees[1] = photos_3;
				otherTrees[2] = photos_4;
				otherTrees[3] = photos_5;
				otherTrees[4] = photos_1;
			}
		}
	}

	public void initializeSet(RBTree photos, Random random, int initial)
	{
		int chunkSize = initial / 8;
		for (int i = 0; i < initial; i += chunkSize)
		{
			addToSet(photos, random, chunkSize, m_max_range, m_min_range,
					m_key_range);
		}
	}

	@Atomic
	public void addToSet(RBTree photos, Random random, int chunkSize,
			int max_range, int min_range, int key_range)
	{
		for (int i = 0; i < chunkSize; i++)
		{
			int m_range = max_range - min_range;
			int width = random.nextInt(m_range + 1) + min_range;
			int height = random.nextInt(m_range + 1) + min_range;

			Object val = new byte[width * height * 3];
			while (!photos.insert(random.nextInt(key_range), val))
				;
		}
	}

	@Atomic
	private boolean initCollection()
	{
		if (photos_1 == null)
			photos_1 = new RBTree();
		if (photos_2 == null)
			photos_2 = new RBTree();
		if (photos_3 == null)
			photos_3 = new RBTree();
		if (photos_4 == null)
			photos_4 = new RBTree();
		if (photos_5 == null)
			photos_5 = new RBTree();
		if (photos_6 == null)
			photos_6 = new RBTree();

		return false;
	}

	public org.deuce.benchmark.BenchmarkThread createThread(int i, int nb)
	{
		return new BenchmarkThread(myTrees, otherTrees, m_max_range,
				m_min_range, m_rate, m_read_rate, m_key_range);
	}

	public String getStats(org.deuce.benchmark.BenchmarkThread[] threads)
	{
		int add = 0;
		int remove = 0;
		int contains_remote = 0;
		int contains_local = 0;
		for (int i = 0; i < threads.length; i++)
		{
			add += ((BenchmarkThread) threads[i]).m_nb_add;
			remove += ((BenchmarkThread) threads[i]).m_nb_remove;
			contains_remote += ((BenchmarkThread) threads[i]).m_nb_contains_remote;
			contains_local += ((BenchmarkThread) threads[i]).m_nb_contains_local;
		}
		return "A=" + add + ", R=" + remove + ", C(r)=" + contains_remote
				+ ", C(l)=" + contains_local;
	}
}
