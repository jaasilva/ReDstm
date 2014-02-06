package org.deuce.benchmark;

/**
 * @author Pascal Felber
 * @since 0.1
 */
public class BenchmarkThread implements Runnable
{
	volatile private int m_phase;
	private int m_steps;

	public BenchmarkThread()
	{
		m_phase = Benchmark.WARMUP_PHASE;
		m_steps = 0;
	}

	public void setPhase(int phase)
	{
		m_phase = phase;
	}

	public int getSteps()
	{
		return m_steps;
	}

	public void run()
	{
		while (m_phase == Benchmark.WARMUP_PHASE)
		{
			step(Benchmark.WARMUP_PHASE);
			System.out.println("warmup_phase");
		}

		while (m_phase == Benchmark.TEST_PHASE)
		{
			step(Benchmark.TEST_PHASE);
			m_steps++;
			System.out.println("test_phase");
		}
		System.out.println("shutdown_phase " + Thread.currentThread().getId());
		return;
	}

	protected void step(int phase)
	{
	}

	public String getStats()
	{
		return "";
	}
}
