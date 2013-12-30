package org.deuce.benchmark;

import org.deuce.Atomic;
import org.deuce.Defaults;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.Bootstrap;
import org.deuce.profiling.Profiler;

/**
 * @author Pascal Felber
 * @since 0.1
 */
public class Driver
{
	@Bootstrap(id = 1)
	static public Barrier setupBarrier;
	@Bootstrap(id = 2)
	static public Barrier finishBarrier;

	static public int n_threads;

	public static void main(String[] args)
	{
		int nb_threads = 8;
		int duration = 10000;
		int warmup = 2000;
		String benchmark = null;
		boolean error = false;
		int arg;
		int partial_ops_priv = 0;

		for (arg = 0; arg < args.length && !error; arg++)
		{
			if (args[arg].equals("-n"))
			{
				if (++arg < args.length)
				{
					nb_threads = Integer.parseInt(args[arg]);
					n_threads = nb_threads;
				}
				else
					error = true;
			}
			else if (args[arg].equals("-d"))
			{
				if (++arg < args.length)
					duration = Integer.parseInt(args[arg]);
				else
					error = true;
			}
			else if (args[arg].equals("-w"))
			{
				if (++arg < args.length)
					warmup = Integer.parseInt(args[arg]);
				else
					error = true;
			}
			else if (args[arg].equals("-po"))
			{
				if (++arg < args.length)
				{
					partial_ops_priv = Integer.parseInt(args[arg]);
				}
				else
					error = true;
			}
			else
				break;
		}
		if (arg < args.length)
		{
			benchmark = args[arg++];
			String[] s = new String[args.length - arg];
			System.arraycopy(args, arg, s, 0, s.length);
			args = s;
		}
		else
			error = true;

		if (error)
		{
			System.out
					.println("Usage: java Driver [-n nb-threads] [-d duration-ms] [-w warmup-ms] benchmark [args...]");
			System.exit(1);
		}

		Benchmark b = null;
		try
		{
			Class<?> c = Class.forName(benchmark);
			b = (Benchmark) c.newInstance();
		}
		catch (Exception e)
		{
			System.err.println("Unexpected exception: " + e.getMessage());
			System.exit(1);
		}

		b.init(args);
		System.setProperty(Defaults.RBTREE_PARTIAL_OPS, "" + partial_ops_priv);

		// try
		// {
		// Thread.sleep(new Random().nextInt(5000));
		// }
		// catch (InterruptedException e)
		// {
		// e.printStackTrace();
		// }
		System.out.println("initBarriers...");
		initBarriers();
		System.out.println("-initBarriers...");
		// try
		// {
		// Thread.sleep(new Random().nextInt(5000));
		// }
		// catch (InterruptedException e)
		// {
		// e.printStackTrace();
		// }
		System.out.println("setupBarrier...");
		setupBarrier.join();
		System.out.println("-setupBarrier...");

		BenchmarkThread[] bt = new BenchmarkThread[nb_threads];
		for (int i = 0; i < bt.length; i++)
			bt[i] = b.createThread(i, bt.length);

		Thread[] t = new Thread[bt.length];
		for (int i = 0; i < t.length; i++)
			t[i] = new Thread(bt[i]);

		for (int i = 0; i < t.length; i++)
		{
			bt[i].setPhase(Benchmark.WARMUP_PHASE);
			t[i].start();
		}

		long wstart = System.currentTimeMillis();
		try
		{
			Thread.sleep(warmup);
		}
		catch (InterruptedException e)
		{
		}
		long wend = System.currentTimeMillis();

		Profiler.enable();
		for (int i = 0; i < bt.length; i++)
		{
			bt[i].setPhase(Benchmark.TEST_PHASE);
		}

		long tstart = System.currentTimeMillis();
		try
		{
			Thread.sleep(duration);
		}
		catch (InterruptedException e)
		{
		}
		long tend = System.currentTimeMillis();

		for (int i = 0; i < bt.length; i++)
		{
			bt[i].setPhase(Benchmark.SHUTDOWN_PHASE);
		}
		System.err.println("done.");

		for (int i = 0; i < t.length; i++)
		{
			try
			{
				t[i].join();
			}
			catch (InterruptedException e)
			{
			}
		}
		Profiler.disable();
		System.err.println("done2.");

		System.out.println();
		System.out.println("All threads returned successfully");

		int steps = 0;
		for (int i = 0; i < bt.length; i++)
			steps += bt[i].getSteps();

		System.out.println("RESULTS:\n");
		System.out.println("  Warmup duration (ms) = " + (wend - wstart));
		System.out.println("  Test duration (ms)   = " + (tend - tstart));
		System.out.println("  Nb iterations        = " + steps);
		System.out.println("  Stats                = " + b.getStats(bt));
		for (int i = 0; i < bt.length; i++)
			System.out.println("    " + i + " : " + bt[i].getSteps() + " ("
					+ bt[i].getStats() + ")");

		Profiler.print();

		// try
		// {
		// Thread.sleep(new Random().nextInt(2000));
		// }
		// catch (InterruptedException e)
		// {
		// e.printStackTrace();
		// }
		finishBarrier.join();

		System.out.println("VALID: " + b.validate());

		TribuDSTM.close();
	}

	@Atomic
	private static void initBarriers()
	{
		if (setupBarrier == null)
			setupBarrier = new Barrier(Integer.getInteger("tribu.replicas"));
		if (finishBarrier == null)
			finishBarrier = new Barrier(Integer.getInteger("tribu.replicas"));
	}
}
