package org.deuce.benchmark;

import org.deuce.Atomic;
import org.deuce.reflection.AddressUtil;

public class Barrier
{
	protected static long counter_offset;
	static
	{
		try
		{
			counter_offset = AddressUtil.getAddress(Barrier.class
					.getDeclaredField("counter"));
		}
		catch (NoSuchFieldException e)
		{
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
	}
	protected volatile int counter;
	protected volatile int expected;

	private static final long pollingPeriod = 500;

	// private transient Random rand = new Random();

	public Barrier(int expected)
	{
		counter = 0;
		this.expected = expected;
	}

	// private void readObject(ObjectInputStream stream) throws IOException,
	// ClassNotFoundException
	// {
	// stream.defaultReadObject();
	// rand = new Random();
	// }

	public void join()
	{
		// try {
		// long sleepValue = (Math.abs(rand.nextLong()) % 10) * 1000;
		// System.err.println("Sleeping for " + (sleepValue / 1000)
		// + " seconds");
		// Thread.sleep(sleepValue);
		// } catch (InterruptedException e1) {
		// e1.printStackTrace();
		// }

		increment();

		System.err.println("Barrier increased to " + counter + " (expected="
				+ expected + ")");
		boolean exit = false;
		while (!exit)
		{
			try
			{
				int c = counter;
				boolean res = c >= expected;
				// System.out.println("##> " + res + " " + c);
				if (res)
				{
					exit = true;
				}

				// if (UnsafeHolder.getUnsafe().getIntVolatile(this,
				// counter_offset) >= expected)
				// {
				// exit = true;
				// }

				Thread.sleep(pollingPeriod);
			}
			catch (InterruptedException e)
			{
				System.err.println("Pao -> who should be interrupting me?");
				e.printStackTrace();
			}
		}

		System.err.println("-- Barrier increased to " + counter + " (expected="
				+ expected + ")");
		// UnsafeHolder.getUnsafe().putIntVolatile(this, counter_offset, 0);
		// resetCounter();
		counter = 0;
	}

	// @Atomic
	// private int getCounter()
	// {
	// return counter;
	// }
	//
	// @Atomic
	// private int getExpected()
	// {
	// return expected;
	// }

	@Atomic
	private void increment()
	{
		counter = counter + 1;
	}
}
