package org.deuce.profiling;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class PRProfiler
{
	public static boolean enabled = false;
	private static Object lock = new Object();
	private static final int THREADS = 8;

	/**
	 * Transaction throughput related.
	 */
	private static long txCommitted, txAborted, txTimeout;

	/**
	 * Incremental average.
	 */
	private static long txDurationIt, txVotesIt, txValidateIt, txCommitIt,
			msgSent, msgRecv, txRReadIt, txLReadIt, txCReadIt, serIt;

	/**
	 * Time related, in nanoseconds.
	 */
	private static long[] txAppDuration = new long[THREADS];
	private static long[] txVotes = new long[THREADS];
	private static long[] txValidate = new long[THREADS];
	private static long[] txCommit = new long[THREADS];
	private static long[] txRRead = new long[THREADS];
	private static long[] txLRead = new long[THREADS];
	private static long[] txCRead = new long[THREADS];
	private static long[] serialization = new long[THREADS];
	private static long txAppDurationAvg, txAppDurationMax = Long.MIN_VALUE,
			txAppDurationMin = Long.MAX_VALUE, txVotesAvg,
			txVotesMax = Long.MIN_VALUE, txVotesMin = Long.MAX_VALUE,
			txValidateAvg, txValidateMax = Long.MIN_VALUE,
			txValidateMin = Long.MAX_VALUE, txCommitAvg,
			txCommitMax = Long.MIN_VALUE, txCommitMin = Long.MAX_VALUE,
			txRReadAvg, txRReadMax = Long.MIN_VALUE,
			txRReadMin = Long.MAX_VALUE, txLReadAvg,
			txLReadMax = Long.MIN_VALUE, txLReadMin = Long.MAX_VALUE,
			txCReadAvg, txCReadMax = Long.MIN_VALUE,
			txCReadMin = Long.MAX_VALUE, serAvg, serMax = Long.MIN_VALUE,
			serMin = Long.MAX_VALUE;

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
			txAppDuration[ctxID] = txAppStart;
		}
	}

	public static void onTxAppFinish(int ctxID)
	{
		if (enabled)
		{
			long txAppEnd = System.nanoTime();
			long elapsed = txAppEnd - txAppDuration[ctxID];

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
			txVotes[ctxID] = txPrepStart;
		}
	}

	public static void onLastVoteDelivery(int ctxID)
	{
		if (enabled)
		{
			long txPrepEnd = System.nanoTime();
			long elapsed = txPrepEnd - txVotes[ctxID];

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
			txValidate[ctxID] = txValidateStart;
		}
	}

	public static void onTxValidateEnd(int ctxID)
	{
		if (enabled)
		{
			long txValidateEnd = System.nanoTime();
			long elapsed = txValidateEnd - txValidate[ctxID];

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
			txCommit[ctxID] = txCommitStart;
		}
	}

	public static void onTxCommitEnd(int ctxID)
	{
		if (enabled)
		{
			long txCommitEnd = System.nanoTime();
			long elapsed = txCommitEnd - txCommit[ctxID];

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

	public static void onTxRemoteReadBegin(int ctxID)
	{
		if (enabled)
		{
			long txReadStart = System.nanoTime();
			txRRead[ctxID] = txReadStart;
		}
	}

	public static void onTxRemoteReadFinish(int ctxID)
	{
		if (enabled)
		{
			long txReadEnd = System.nanoTime();
			long elapsed = txReadEnd - txRRead[ctxID];

			synchronized (lock)
			{
				if (elapsed < txRReadMin)
				{
					txRReadMin = elapsed;
				}
				else if (elapsed > txRReadMax)
				{
					txRReadMax = elapsed;
				}

				txRReadAvg = incAvg(txRReadAvg, elapsed, txRReadIt);
				txRReadIt++;
			}
		}
	}

	public static void onTxLocalReadBegin(int ctxID)
	{
		if (enabled)
		{
			long txReadStart = System.nanoTime();
			txLRead[ctxID] = txReadStart;
		}
	}

	public static void onTxLocalReadFinish(int ctxID)
	{
		if (enabled)
		{
			long txReadEnd = System.nanoTime();
			long elapsed = txReadEnd - txLRead[ctxID];

			synchronized (lock)
			{
				if (elapsed < txLReadMin)
				{
					txLReadMin = elapsed;
				}
				else if (elapsed > txLReadMax)
				{
					txLReadMax = elapsed;
				}

				txLReadAvg = incAvg(txLReadAvg, elapsed, txLReadIt);
				txLReadIt++;
			}
		}
	}

	public static void onTxCompleteReadBegin(int ctxID)
	{
		if (enabled)
		{
			long txReadStart = System.nanoTime();
			txCRead[ctxID] = txReadStart;
		}
	}

	public static void onTxCompleteReadFinish(int ctxID)
	{
		if (enabled)
		{
			long txReadEnd = System.nanoTime();
			long elapsed = txReadEnd - txCRead[ctxID];

			synchronized (lock)
			{
				if (elapsed < txCReadMin)
				{
					txCReadMin = elapsed;
				}
				else if (elapsed > txCReadMax)
				{
					txCReadMax = elapsed;
				}

				txCReadAvg = incAvg(txCReadAvg, elapsed, txCReadIt);
				txCReadIt++;
			}
		}
	}

	public static void onSerializationBegin(int ctxID)
	{
		if (enabled)
		{
			long serStart = System.nanoTime();
			serialization[ctxID] = serStart;
		}
	}

	public static void onSerializationFinish(int ctxID)
	{
		if (enabled)
		{
			long serEnd = System.nanoTime();
			long elapsed = serEnd - serialization[ctxID];

			synchronized (lock)
			{
				if (elapsed < serMin)
				{
					serMin = elapsed;
				}
				else if (elapsed > serMax)
				{
					serMax = elapsed;
				}

				serAvg = incAvg(serAvg, elapsed, serIt);
				serIt++;
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
		stats.append("\tComplete Reads\n");
		stats.append("\t\tavg = " + txCReadAvg / 1000 + " µs\n");
		stats.append("\t\tmax = " + txCReadMax / 1000 + " µs\n");
		stats.append("\t\tmin = " + txCReadMin / 1000 + " µs\n");
		stats.append("\tRemote Reads\n");
		stats.append("\t\tavg = " + txRReadAvg / 1000000 + " ms\n");
		stats.append("\t\tmax = " + txRReadMax / 1000000 + " ms\n");
		stats.append("\t\tmin = " + txRReadMin / 1000000 + " ms\n");
		stats.append("\tLocal Reads\n");
		stats.append("\t\tavg = " + txLReadAvg / 1000 + " µs\n");
		stats.append("\t\tmax = " + txLReadMax / 1000 + " µs\n");
		stats.append("\t\tmin = " + txLReadMin / 1000 + " µs\n");
		stats.append("\tSerialization\n");
		stats.append("\t\tavg = " + serAvg / 1000 + " µs\n");
		stats.append("\t\tmax = " + serMax / 1000 + " µs\n");
		stats.append("\t\tmin = " + serMin / 1000 + " µs\n");

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
