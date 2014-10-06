package org.deuce.benchmark.intset;

import java.util.Random;

import org.deuce.Atomic;

/**
 * @author Pascal Felber
 * @since 0.1
 */
public class BenchmarkThread3 extends org.deuce.benchmark.BenchmarkThread
{
	private static final int TIMES = 5;
	final private IntSet m_set;
	final private int m_range;
	int m_nb_add;
	int m_nb_remove;
	int m_nb_contains;
	final private int m_rate;
	boolean m_write;
	final private Random m_random;
	final private int max, min;

	public BenchmarkThread3(IntSet set, int range, int rate, int max, int min)
	{
		m_set = set;
		m_range = range;
		m_nb_add = m_nb_remove = m_nb_contains = 0;
		m_rate = rate;
		m_write = true;
		m_random = new Random();
		this.max = max;
		this.min = min;
	}

	protected void step(int phase)
	{
		int i = m_random.nextInt(100);
		if (i < m_rate)
		{
			if (m_write)
			{
				addTen();
				if (phase == Benchmark.TEST_PHASE)
					m_nb_add++;
				m_write = false;
			}
			else
			{
				removeTen();
				if (phase == Benchmark.TEST_PHASE)
					m_nb_remove++;
				m_write = true;
			}
		}
		else
		{
			containsTen();
			if (phase == Benchmark.TEST_PHASE)
				m_nb_contains++;
		}
	}

	@Atomic
	private void addTen()
	{
		for (int j = 0; j < TIMES; ++j)
			m_set.add(m_random.nextInt((max - min)) + min);
	}

	@Atomic
	private void removeTen()
	{
		for (int j = 0; j < TIMES; ++j)
			m_set.remove(m_random.nextInt((max - min)) + min);
	}

	@Atomic
	private void containsTen()
	{
		for (int j = 0; j < TIMES; ++j)
			m_set.contains(m_random.nextInt((max - min)) + min);
	}

	public String getStats()
	{
		return "A=" + m_nb_add + ", R=" + m_nb_remove + ", C=" + m_nb_contains;
	}
}
