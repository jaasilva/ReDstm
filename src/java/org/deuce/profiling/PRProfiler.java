package org.deuce.profiling;

import java.util.HashMap;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class PRProfiler
{
	public static boolean enabled = false;
	private static Object lock = new Object();

	/**
	 * Transaction throughput related.
	 */
	private static long txCommitted, txAborted, txTimeout;

	/**
	 * Incremental average.
	 */
	private static long txDurationIt, txVotesIt, txValidateIt, txCommitIt,
			msgSent, msgRecv, txReadIt;

	/**
	 * Time related, in nanoseconds.
	 */
	private static HashMap<Integer, Long> txAppDuration = new HashMap<Integer, Long>();
	private static HashMap<Integer, Long> txVotes = new HashMap<Integer, Long>();
	private static HashMap<Integer, Long> txValidate = new HashMap<Integer, Long>();
	private static HashMap<Integer, Long> txCommit = new HashMap<Integer, Long>();
	private static HashMap<Integer, Long> txRead = new HashMap<Integer, Long>();
	private static long txAppDurationAvg, txAppDurationMax = Long.MIN_VALUE,
			txAppDurationMin = Long.MAX_VALUE, txVotesAvg,
			txVotesMax = Long.MIN_VALUE, txVotesMin = Long.MAX_VALUE,
			txValidateAvg, txValidateMax = Long.MIN_VALUE,
			txValidateMin = Long.MAX_VALUE, txCommitAvg,
			txCommitMax = Long.MIN_VALUE, txCommitMin = Long.MAX_VALUE,
			txReadAvg, txReadMax = Long.MIN_VALUE, txReadMin = Long.MAX_VALUE;

	/**
	 * Network related, in bytes.
	 */
	private static long msgSentSizeAvg, msgSentSizeMax = Long.MIN_VALUE,
			msgSentSizeMin = Long.MAX_VALUE, msgRecvSizeAvg,
			msgRecvSizeMax = Long.MIN_VALUE, msgRecvSizeMin = Long.MAX_VALUE;

	private static long incAvg(long currAvg, long value, long currIt)
	{
		return currAvg + ((value - currAvg) / (currIt + 1));
	}

	public static void txCommitted()
	{
		if (enabled)
		{
			txCommitted++;
		}
	}

	public static void txAborted()
	{
		if (enabled)
		{
			txAborted++;
		}
	}

	public static void txProcessed(boolean committed)
	{
		if (enabled)
		{
			if (committed)
			{
				txCommitted();
			}
			else
			{
				txAborted();
			}
		}
	}

	public static void txTimeout()
	{
		if (enabled)
		{
			txTimeout++;
		}
	}

	public static void onTxAppBegin(int ctxID)
	{
		if (enabled)
		{
			long txAppStart = System.nanoTime();
			txAppDuration.put(ctxID, txAppStart);
		}
	}

	public static void onTxAppFinish(int ctxID)
	{
		if (enabled)
		{
			long txAppEnd = System.nanoTime();
			long elapsed = txAppEnd - txAppDuration.get(ctxID);

			synchronized (lock)
			{
				if (elapsed < txAppDurationMin)
				{
					txAppDurationMin = elapsed;
				}
				else if (elapsed > txAppDurationMax)
				{
					txAppDurationMax = elapsed;
				}

				txAppDurationAvg = incAvg(txAppDurationAvg, elapsed,
						txDurationIt);
				txDurationIt++;
			}
		}
	}

	public static void onPrepSend(int ctxID)
	{
		if (enabled)
		{
			long txPrepStart = System.nanoTime();
			txVotes.put(ctxID, txPrepStart);
		}
	}

	public static void onLastVoteDelivery(int ctxID)
	{
		if (enabled)
		{
			long txPrepEnd = System.nanoTime();
			long elapsed = txPrepEnd - txVotes.get(ctxID);

			synchronized (lock)
			{
				if (elapsed < txVotesMin)
				{
					txVotesMin = elapsed;
				}
				else if (elapsed > txVotesMax)
				{
					txVotesMax = elapsed;
				}

				txVotesAvg = incAvg(txVotesAvg, elapsed, txVotesIt);
				txVotesIt++;
			}
		}
	}

	public static void onTxValidateBegin(int ctxID)
	{
		if (enabled)
		{
			long txValidateStart = System.nanoTime();
			txValidate.put(ctxID, txValidateStart);
		}
	}

	public static void onTxValidateEnd(int ctxID)
	{
		if (enabled)
		{
			long txValidateEnd = System.nanoTime();
			long elapsed = txValidateEnd - txValidate.get(ctxID);

			synchronized (lock)
			{
				if (elapsed < txValidateMin)
				{
					txValidateMin = elapsed;
				}
				else if (elapsed > txValidateMax)
				{
					txValidateMax = elapsed;
				}

				txValidateAvg = incAvg(txValidateAvg, elapsed, txValidateIt);
				txValidateIt++;
			}
		}
	}

	public static void onTxCommitBegin(int ctxID)
	{
		if (enabled)
		{
			long txCommitStart = System.nanoTime();
			txCommit.put(ctxID, txCommitStart);
		}
	}

	public static void onTxCommitEnd(int ctxID)
	{
		if (enabled)
		{
			long txCommitEnd = System.nanoTime();
			long elapsed = txCommitEnd - txCommit.get(ctxID);

			synchronized (lock)
			{
				if (elapsed < txCommitMin)
				{
					txCommitMin = elapsed;
				}
				else if (elapsed > txCommitMax)
				{
					txCommitMax = elapsed;
				}

				txCommitAvg = incAvg(txCommitAvg, elapsed, txCommitIt);
				txCommitIt++;
			}
		}
	}

	public static void newMsgSent(int bytes)
	{
		if (enabled)
		{
			if (bytes < msgSentSizeMin)
			{
				msgSentSizeMin = bytes;
			}
			else if (bytes > msgSentSizeMax)
			{
				msgSentSizeMax = bytes;
			}

			msgSentSizeAvg = incAvg(msgSentSizeAvg, bytes, msgSent);
			msgSent++;
		}
	}

	public static void newMsgRecv(int bytes)
	{
		if (enabled)
		{
			if (bytes < msgRecvSizeMin)
			{
				msgRecvSizeMin = bytes;
			}
			else if (bytes > msgRecvSizeMax)
			{
				msgRecvSizeMax = bytes;
			}

			msgRecvSizeAvg = incAvg(msgRecvSizeAvg, bytes, msgRecv);
			msgRecv++;
		}
	}

	public static void onTxReadBegin(int ctxID)
	{
		if (enabled)
		{
			long txReadStart = System.nanoTime();
			txRead.put(ctxID, txReadStart);
		}
	}

	public static void onTxReadFinish(int ctxID)
	{
		if (enabled)
		{
			long txReadEnd = System.nanoTime();
			long elapsed = txReadEnd - txRead.get(ctxID);

			synchronized (lock)
			{
				if (elapsed < txReadMin)
				{
					txReadMin = elapsed;
				}
				else if (elapsed > txReadMax)
				{
					txReadMax = elapsed;
				}

				txReadAvg = incAvg(txReadAvg, elapsed, txReadIt);
				txReadIt++;
			}
		}
	}

	public static void print()
	{
		StringBuffer stats = new StringBuffer();

		stats.append("\n########## STATISTICS ##########\n");
		stats.append("Committed: " + txCommitted + " Aborted: " + txAborted
				+ " Timeout: " + txTimeout + "\n");

		stats.append("=== Transactions\n");
		stats.append("\tTx app duration\n");
		stats.append("\t\tavg = " + txAppDurationAvg / 1000 + " µs\n");
		stats.append("\t\tmax = " + txAppDurationMax / 1000 + " µs\n");
		stats.append("\t\tmin = " + txAppDurationMin / 1000 + " µs\n");
		stats.append("\tValidation\n");
		stats.append("\t\tavg = " + txValidateAvg / 1000 + " µs\n");
		stats.append("\t\tmax = " + txValidateMax / 1000 + " µs\n");
		stats.append("\t\tmin = " + txValidateMin / 1000 + " µs\n");
		stats.append("\tCommit\n");
		stats.append("\t\tavg = " + txCommitAvg / 1000 + " µs\n");
		stats.append("\t\tmax = " + txCommitMax / 1000 + " µs\n");
		stats.append("\t\tmin = " + txCommitMin / 1000 + " µs\n");
		stats.append("\tRemote Reads\n");
		stats.append("\t\tavg = " + txReadAvg / 1000000 + " ms\n");
		stats.append("\t\tmax = " + txReadMax / 1000000 + " ms\n");
		stats.append("\t\tmin = " + txReadMin / 1000000 + " ms\n");

		stats.append("\n");

		stats.append("=== Network\n");
		stats.append("\tWaiting for votes\n");
		stats.append("\t\tavg = " + txVotesAvg / 1000000 + " ms\n");
		stats.append("\t\tmax = " + txVotesMax / 1000000 + " ms\n");
		stats.append("\t\tmin = " + txVotesMin / 1000000 + " ms\n");
		stats.append("\tMsgs sent = " + msgSent + "\n");
		stats.append("\t\tavg = " + msgSentSizeAvg + " bytes\n");
		stats.append("\t\tmax = " + msgSentSizeMax + " bytes\n");
		stats.append("\t\tmin = " + msgSentSizeMin + " bytes\n");
		stats.append("\tMsgs recv = " + msgRecv + "\n");
		stats.append("\t\tavg = " + msgRecvSizeAvg + " bytes\n");
		stats.append("\t\tmax = " + msgRecvSizeMax + " bytes\n");
		stats.append("\t\tmin = " + msgRecvSizeMin + " bytes\n");

		System.out.println(stats.toString());
	}
}
