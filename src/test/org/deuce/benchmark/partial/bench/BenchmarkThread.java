package org.deuce.benchmark.partial.bench;

import java.util.Random;

import org.deuce.Atomic;
import org.deuce.benchmark.partial.photos.Benchmark;

/**
 * @author jaasilva
 */
public class BenchmarkThread extends org.deuce.benchmark.BenchmarkThread
{
	int m_nb_add;
	int m_nb_remove;
	int m_nb_contains_remote, m_nb_contains_local;

	final private RedBTree[] remote;
	final private RedBTree local;
	final private int m_range;
	final private int m_rate;
	final private int m_remote_read;
	boolean m_write;
	final private Random m_random;

	public BenchmarkThread(RedBTree _local, RedBTree[] _remote, int rate,
			int range, int remote_read)
	{
		local = _local;
		remote = _remote;
		m_nb_add = m_nb_remove = m_nb_contains_remote = m_nb_contains_local = 0;
		m_rate = rate;
		m_remote_read = remote_read;
		m_range = range;
		m_write = true;
		m_random = new Random();
	}

	protected void step(int phase)
	{
		int i = m_random.nextInt(100);
		if (i < m_rate)
		{ // ############# WRITE
			if (m_write)
			{ // ######################## ADD
				m_write = false;
				addTen();
				if (phase == Benchmark.TEST_PHASE)
					m_nb_add++;
			}
			else
			{ // ######################## REMOVE
				m_write = true;
				removeTen();
				if (phase == Benchmark.TEST_PHASE)
					m_nb_remove++;
			}
		}
		else
		{ // ############# READ
			int j = m_random.nextInt(100);
			if (j < m_remote_read)
			{ // ######################## REMOTE
				int rbt = m_random.nextInt(remote.length);
				containsRemoteTen(rbt);
				if (phase == Benchmark.TEST_PHASE)
					m_nb_contains_remote++;
			}
			else
			{ // ######################## LOCAL
				containsLocalTen();
				if (phase == Benchmark.TEST_PHASE)
					m_nb_contains_local++;
			}
		}
	}

	@Atomic
	private void addTen()
	{
		for (int j = 0; j < 10; ++j)
			local.add(m_random.nextInt(m_range));
	}

	@Atomic
	private void removeTen()
	{
		for (int j = 0; j < 10; ++j)
			local.remove(m_random.nextInt(m_range));
	}

	@Atomic
	private void containsLocalTen()
	{
		for (int j = 0; j < 10; ++j)
			local.contains(m_random.nextInt(m_range));
	}

	@Atomic
	private void containsRemoteTen(int rbt)
	{
		for (int j = 0; j < 10; ++j)
			remote[rbt].random_lookup();
	}

	public String getStats()
	{
		return "A=" + m_nb_add + ", R=" + m_nb_remove + ", C(r)="
				+ m_nb_contains_remote + ", C(l)=" + m_nb_contains_local;
	}
}
