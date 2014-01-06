package org.deuce.benchmark.bank;

import java.util.Random;

import org.deuce.Atomic;

/**
 * @author Pascal Felber
 * @since 0.1
 */
public class BenchmarkThread extends org.deuce.benchmark.BenchmarkThread
{

	final private int m_id;
	final private int m_nb;
	final private Account[] m_accounts;
	final private int m_max;
	final private int m_read_frequency;
	final private int m_write_frequency;
	final private int m_read_threads;
	final private int m_write_threads;
	int m_nb_transfers;
	int m_nb_reads;
	int m_nb_writes;
	final private Random m_random;

	final private int replica;
	final private int acc_range;

	BenchmarkThread(int id, int nb, Account[] accounts, int max,
			int read_frequency, int write_frequency, int read_threads,
			int write_threads)
	{
		replica = Integer.getInteger("tribu.site") - 1;
		acc_range = accounts.length
				/ (nb * Integer.getInteger("tribu.replicas"));
		m_id = id;
		m_nb = nb;
		m_accounts = accounts;
		m_max = max;
		m_read_frequency = read_frequency;
		m_write_frequency = write_frequency;
		m_read_threads = read_threads;
		m_write_threads = write_threads;
		m_nb_transfers = m_nb_reads = m_nb_writes = 0;
		m_random = new Random();
	}

	protected void step(int phase)
	{

		if (m_id < m_read_threads)
		{
			// Compute total of all accounts (read-all transaction)
			Account.computeTotal(m_accounts);
			if (phase == Benchmark.TEST_PHASE)
				m_nb_reads++;
		}
		else if (m_id < m_read_threads + m_write_threads)
		{
			// Add 0% interest (write-all transaction)
			Account.addInterest(m_accounts, 0);
			if (phase == Benchmark.TEST_PHASE)
				m_nb_writes++;
		}
		else
		{
			int i = m_random.nextInt(100);
			if (i < m_read_frequency)
			{
				// Compute total of all accounts (read-all transaction)
				Account.computeTotal(m_accounts);
				if (phase == Benchmark.TEST_PHASE)
					m_nb_reads++;
			}
			else if (i < m_read_frequency + m_write_frequency)
			{
				// Add 0% interest (write-all transaction)
				Account.addInterest(m_accounts, 0);
				if (phase == Benchmark.TEST_PHASE)
					m_nb_writes++;
			}
			else
			{
				int amount = m_random.nextInt(m_max) + 1;
				// if (Account.s_disjoint && m_nb <= m_accounts.length) {
				// src = m_accounts[m_random.nextInt(m_accounts.length / m_nb) *
				// m_nb + m_id];
				// dst = m_accounts[m_random.nextInt(m_accounts.length / m_nb) *
				// m_nb + m_id];
				// } else {
				// src = m_accounts[m_random.nextInt(m_accounts.length)];
				// dst = m_accounts[m_random.nextInt(m_accounts.length)];
				// }
				// i <-> m_id
				// j <-> replica
				// numThreads <-> m_nb
				// 10.000 <-> acc_range

				int start = (m_id + replica * m_nb) * acc_range;
				int end = (1 + m_id + replica * m_nb) * acc_range;
				// int j = 0;
				// int aux = 0;
				// for (j = start; j < end - 3; j++) {
				// aux += Account.getBalanceAtomic(m_accounts[j]);
				// }
				// src = m_accounts[j];
				// dst = m_accounts[j+1];
				// try {
				// Account.transfer(src, dst, amount);
				// if (phase == Benchmark.TEST_PHASE)
				// m_nb_transfers++;
				// } catch (OverdraftException e) {
				// System.err.println("Overdraft: " + e.getMessage());
				// }
				try
				{
					lotsOfReadsTwoWrites(start, end, amount);
					if (phase == Benchmark.TEST_PHASE)
						m_nb_transfers++;
				}
				catch (OverdraftException e)
				{
					System.err.println("Overdraft: " + e.getMessage());
				}
			}
		}
	}

	@Atomic
	public void lotsOfReadsTwoWrites(int start, int end, int amount)
			throws OverdraftException
	{
		Account src;
		Account dst;
		int j = 0;
		// int aux = 0;
		for (j = start; j < end - 3; j++)
		{
			// aux += Account.getBalanceAtomic(m_accounts[j]);
			src = m_accounts[j];
		}
		src = m_accounts[j];
		dst = m_accounts[j + 1];
		Account.transfer(src, dst, amount);
	}

	public String getStats()
	{
		return "T=" + m_nb_transfers + ", R=" + m_nb_reads + ", W="
				+ m_nb_writes;
	}
}
