package org.deuce.benchmark;

// import papi.j.CacheMonitor;

/**
 * @author Pascal Felber
 * @since 0.1
 */
abstract public class BenchmarkThread implements Runnable
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
		// CacheMonitor cm = new CacheMonitor(); //RJFD
		// cm.init();
		// cm.monitor_L2();

		// System.err.println(this+": beginning warmup.");

		while (m_phase == Benchmark.WARMUP_PHASE)
		{
			// System.err.print(this+": warmup step...");
			step(Benchmark.WARMUP_PHASE);
			// System.err.println(" done.");
		}

		// System.err.println(this+": finished warmup/beginning test.");

		// cm.start();
		// cm.reset();

		while (m_phase == Benchmark.TEST_PHASE)
		{
			// System.err.print(this+": test step...");
			step(Benchmark.TEST_PHASE);
			m_steps++;
			// System.err.println(" done.");
		}

		// System.err.println(this+": finished working.");

		// cm.stop();
		//
		// cm.close();
	}

	abstract protected void step(int phase);

	abstract public String getStats();
}
