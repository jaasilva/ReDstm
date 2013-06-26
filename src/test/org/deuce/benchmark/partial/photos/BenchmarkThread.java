package org.deuce.benchmark.partial.photos;

import java.util.Random;

public class BenchmarkThread extends org.deuce.benchmark.BenchmarkThread
{
	final private RBTree[] my_photos;
	final private RBTree[] other_photos;
	final private int m_range, m_min_range, m_key_range;
	int m_nb_add;
	int m_nb_remove;
	int m_nb_contains_remote, m_nb_contains_local;
	final private int m_rate, m_read_rate;
	boolean m_write;
	final private Random m_random; // rand.nextInt(max - min + 1) + min

	public BenchmarkThread(RBTree[] myTrees, RBTree[] otherTrees,
			int max_range, int min_range, int rate, int read_rate, int key_range)
	{
		my_photos = myTrees;
		other_photos = otherTrees;
		m_range = max_range - min_range;
		m_min_range = min_range;
		m_key_range = key_range;
		m_rate = rate;
		m_read_rate = read_rate;
		m_nb_add = m_nb_remove = m_nb_contains_remote = m_nb_contains_local = 0;
		m_write = true;
		m_random = new Random();
	}

	protected void step(int phase)
	{
		int i = m_random.nextInt(100);
		if (i < m_rate)
		{
			int photo = m_random.nextInt(my_photos.length);
			if (m_write)
			{
				System.out.println("add");
				m_write = false;
				int width = m_random.nextInt(m_range + 1) + m_min_range;
				int height = m_random.nextInt(m_range + 1) + m_min_range;
				Object val = new MyObject();
				my_photos[photo].insert(m_random.nextInt(m_key_range), val);

				if (phase == Benchmark.TEST_PHASE)
					m_nb_add++;
			}
			else
			{
				System.out.println("remove");
				my_photos[photo].remove(m_random.nextInt(m_key_range));

				if (phase == Benchmark.TEST_PHASE)
					m_nb_remove++;
				m_write = true;
			}
		}
		else
		{
			int j = m_random.nextInt(100);
			if (j < m_read_rate)
			{
				System.out.println("remote read");
				int photo = m_random.nextInt(other_photos.length);
				while (other_photos[photo].find(m_random.nextInt(m_key_range)) == null)
				{
					if (phase == Benchmark.TEST_PHASE)
						m_nb_contains_remote++;
				}
			}
			else
			{
				System.out.println("local read");
				int photo = m_random.nextInt(my_photos.length);
				my_photos[photo].find(m_random.nextInt(m_key_range));

				if (phase == Benchmark.TEST_PHASE)
					m_nb_contains_local++;
			}
		}
	}

	public String getStats()
	{
		return "A=" + m_nb_add + ", R=" + m_nb_remove + ", C(r)="
				+ m_nb_contains_remote + ", C(l)=" + m_nb_contains_local;
	}
}
