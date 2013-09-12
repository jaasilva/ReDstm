package org.deuce.profiling;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class PRProfiler
{
	public static boolean enabled = false;
	private static Object lock = new Object();
	private static final int THREADS = 16;

	/**
	 * Transaction throughput related.
	 */
	private static long txCommitted = 0, txAborted = 0, txTimeout = 0;

	/**
	 * Incremental average.
	 */
	private static long txDurationIt = 0, txVotesIt = 0, txValidateIt = 0,
			txCommitIt = 0, msgSent = 0, msgRecv = 0, txRReadIt = 0,
			txLReadIt = 0, txCReadIt = 0, serIt = 0, waitingReadIt = 0,
			applyWsIt = 0, distCommitIt = 0;

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
	private static long[] applyWs = new long[THREADS];
	private static long[] distCommit = new long[THREADS];
	private static long txAppDurationAvg = 0,
			txAppDurationMax = Long.MIN_VALUE,
			txAppDurationMin = Long.MAX_VALUE, txVotesAvg = 0,
			txVotesMax = Long.MIN_VALUE, txVotesMin = Long.MAX_VALUE,
			txValidateAvg = 0, txValidateMax = Long.MIN_VALUE,
			txValidateMin = Long.MAX_VALUE, txCommitAvg = 0,
			txCommitMax = Long.MIN_VALUE, txCommitMin = Long.MAX_VALUE,
			txRReadAvg = 0, txRReadMax = Long.MIN_VALUE,
			txRReadMin = Long.MAX_VALUE, txLReadAvg = 0,
			txLReadMax = Long.MIN_VALUE, txLReadMin = Long.MAX_VALUE,
			txCReadAvg = 0, txCReadMax = Long.MIN_VALUE,
			txCReadMin = Long.MAX_VALUE, serAvg = 0, serMax = Long.MIN_VALUE,
			serMin = Long.MAX_VALUE, waitingReadAvg = 0,
			waitingReadMax = Long.MIN_VALUE, waitingReadMin = Long.MAX_VALUE,
			applyWsAvg = 0, applyWsMax = Long.MIN_VALUE,
			applyWsMin = Long.MAX_VALUE, txLocalRead = 0, txRemoteRead = 0,
			txReads = 0, txWsReads = 0, distCommitMax = Long.MIN_VALUE,
			distCommitMin = Long.MAX_VALUE, distCommitAvg = 0, totalCommit = 0;

	/**
	 * Network related, in bytes.
	 */
	private static long msgSentSizeAvg = 0, msgSentSizeMax = Long.MIN_VALUE,
			msgSentSizeMin = Long.MAX_VALUE, msgRecvSizeAvg = 0,
			msgRecvSizeMax = Long.MIN_VALUE, msgRecvSizeMin = Long.MAX_VALUE;

	private synchronized static long incAvg(long currAvg, long value,
			long currIt)
	{
		return currAvg + ((value - currAvg) / (currIt + 1));
	}

	private synchronized static void txCommitted()
	{
		if (enabled)
		{
			txCommitted++;
		}
	}

	private synchronized static void txAborted()
	{
		if (enabled)
		{
			txAborted++;
		}
	}

	public synchronized static void txProcessed(boolean committed)
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

	public synchronized static void txTimeout()
	{
		if (enabled)
		{
			txTimeout++;
		}
	}

	public synchronized static void onTxAppBegin(int ctxID)
	{
		if (enabled)
		{
			long txAppStart = System.nanoTime();
			txAppDuration[ctxID] = txAppStart;
		}
	}

	public synchronized static void onTxAppFinish(int ctxID)
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
				if (elapsed > txAppDurationMax)
				{
					txAppDurationMax = elapsed;
				}

				txAppDurationAvg = incAvg(txAppDurationAvg, elapsed,
						txDurationIt);
				txDurationIt++;
			}
		}
	}

	public synchronized static void onPrepSend(int ctxID)
	{
		if (enabled)
		{
			long txPrepStart = System.nanoTime();
			txVotes[ctxID] = txPrepStart;
		}
	}

	public synchronized static void onLastVoteReceived(int ctxID)
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
				if (elapsed > txVotesMax)
				{
					txVotesMax = elapsed;
				}

				txVotesAvg = incAvg(txVotesAvg, elapsed, txVotesIt);
				txVotesIt++;
			}
		}
	}

	public synchronized static void onTxValidateBegin(int ctxID)
	{
		if (enabled)
		{
			long txValidateStart = System.nanoTime();
			txValidate[ctxID] = txValidateStart;
		}
	}

	public synchronized static void onTxValidateEnd(int ctxID)
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
				if (elapsed > txValidateMax)
				{
					txValidateMax = elapsed;
				}

				txValidateAvg = incAvg(txValidateAvg, elapsed, txValidateIt);
				txValidateIt++;
			}
		}
	}

	public synchronized static void onTxCommitBegin(int ctxID)
	{
		if (enabled)
		{
			long txCommitStart = System.nanoTime();
			txCommit[ctxID] = txCommitStart;
		}
	}

	public synchronized static void onTxCommitEnd(int ctxID)
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
				if (elapsed > txCommitMax)
				{
					txCommitMax = elapsed;
				}

				txCommitAvg = incAvg(txCommitAvg, elapsed, txCommitIt);
				txCommitIt++;
			}
		}
	}

	public synchronized static void newMsgSent(int bytes)
	{
		if (enabled)
		{
			if (bytes < msgSentSizeMin)
			{
				msgSentSizeMin = bytes;
			}
			if (bytes > msgSentSizeMax)
			{
				msgSentSizeMax = bytes;
			}

			msgSentSizeAvg = incAvg(msgSentSizeAvg, bytes, msgSent);
			msgSent++;
		}
	}

	public synchronized static void newMsgRecv(int bytes)
	{
		if (enabled)
		{
			if (bytes < msgRecvSizeMin)
			{
				msgRecvSizeMin = bytes;
			}
			if (bytes > msgRecvSizeMax)
			{
				msgRecvSizeMax = bytes;
			}

			msgRecvSizeAvg = incAvg(msgRecvSizeAvg, bytes, msgRecv);
			msgRecv++;
		}
	}

	public synchronized static void onTxRemoteReadBegin(int ctxID)
	{
		if (enabled)
		{
			long txReadStart = System.nanoTime();
			txRRead[ctxID] = txReadStart;
			txRemoteRead++;
		}
	}

	public synchronized static void onTxRemoteReadFinish(int ctxID)
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
				if (elapsed > txRReadMax)
				{
					txRReadMax = elapsed;
				}

				txRReadAvg = incAvg(txRReadAvg, elapsed, txRReadIt);
				txRReadIt++;
			}
		}
	}

	public synchronized static void onTxLocalReadBegin(int ctxID)
	{
		if (enabled)
		{
			long txReadStart = System.nanoTime();
			txLRead[ctxID] = txReadStart;
			txLocalRead++;
		}
	}

	public synchronized static void onTxLocalReadFinish(int ctxID)
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
				if (elapsed > txLReadMax)
				{
					txLReadMax = elapsed;
				}

				txLReadAvg = incAvg(txLReadAvg, elapsed, txLReadIt);
				txLReadIt++;
			}
		}
	}

	public synchronized static void onTxCompleteReadBegin(int ctxID)
	{
		if (enabled)
		{
			long txReadStart = System.nanoTime();
			txCRead[ctxID] = txReadStart;
			txReads++;
		}
	}

	public synchronized static void onTxCompleteReadFinish(int ctxID)
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
				if (elapsed > txCReadMax)
				{
					txCReadMax = elapsed;
				}

				txCReadAvg = incAvg(txCReadAvg, elapsed, txCReadIt);
				txCReadIt++;
			}
		}
	}

	public synchronized static void onTxWsRead(int ctxID)
	{
		if (enabled)
		{
			txReads++;
			txWsReads++;
		}
	}

	public synchronized static void onSerializationBegin(int ctxID)
	{
		if (enabled)
		{
			long serStart = System.nanoTime();
			serialization[ctxID] = serStart;
		}
	}

	public synchronized static void onSerializationFinish(int ctxID)
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
				if (elapsed > serMax)
				{
					serMax = elapsed;
				}

				serAvg = incAvg(serAvg, elapsed, serIt);
				serIt++;
			}
		}
	}

	public synchronized static void onWaitingReadFinish(long time)
	{
		if (enabled)
		{
			synchronized (lock)
			{
				if (time < waitingReadMin)
				{
					waitingReadMin = time;
				}
				if (time > waitingReadMax)
				{
					waitingReadMax = time;
				}

				waitingReadAvg = incAvg(waitingReadAvg, time, waitingReadIt);
				waitingReadIt++;
			}
		}
	}

	public synchronized static void onApplyWsBegin(int ctxID)
	{
		if (enabled)
		{
			long applyStart = System.nanoTime();
			applyWs[ctxID] = applyStart;
		}
	}

	public synchronized static void onApplyWsFinish(int ctxID)
	{
		if (enabled)
		{
			long applyEnd = System.nanoTime();
			long elapsed = applyEnd - applyWs[ctxID];

			synchronized (lock)
			{
				if (elapsed < applyWsMin)
				{
					applyWsMin = elapsed;
				}
				if (elapsed > applyWsMax)
				{
					applyWsMax = elapsed;
				}

				applyWsAvg = incAvg(applyWsAvg, elapsed, applyWsIt);
				applyWsIt++;
			}
		}
	}

	public synchronized static void onTxDistCommitBegin(int ctxID)
	{
		if (enabled)
		{
			long start = System.nanoTime();
			distCommit[ctxID] = start;
		}
	}

	public synchronized static void onTxDistCommitFinish(int ctxID)
	{
		if (enabled)
		{
			long end = System.nanoTime();
			long elapsed = end - distCommit[ctxID];

			synchronized (lock)
			{
				if (elapsed < distCommitMin)
				{
					distCommitMin = elapsed;
				}
				if (elapsed > distCommitMax)
				{
					distCommitMax = elapsed;
				}

				distCommitAvg = incAvg(distCommitAvg, elapsed, distCommitIt);
				distCommitIt++;
			}
		}
	}

	public synchronized static void whatNodes(int nodes)
	{
		if (enabled)
		{
			if (nodes == Integer.getInteger("tribu.replicas"))
			{
				totalCommit++;
			}
		}
	}

	public synchronized static void print()
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
		stats.append("\tDistributed Commit " + distCommitIt + " " + totalCommit
				+ "\n");
		stats.append("\t\tavg = " + distCommitAvg / 1000000 + " ms\n");
		stats.append("\t\tmax = " + distCommitMax / 1000000 + " ms\n");
		stats.append("\t\tmin = " + distCommitMin / 1000000 + " ms\n");
		stats.append("\tComplete reads " + txReads + "\n");
		stats.append("\t\tCORRECT: "
				+ (txReads == (txWsReads + txRemoteRead + txLocalRead)) + "\n");
		NumberFormat formatter = new DecimalFormat("#.#####");
		String percent = formatter.format((txWsReads * 100.0) / txReads);
		stats.append("\t\tWS hits = " + txWsReads + " " + percent + "\n");
		stats.append("\t\tavg = " + txCReadAvg / 1000 + " µs\n");
		stats.append("\t\tmax = " + txCReadMax / 1000 + " µs\n");
		stats.append("\t\tmin = " + txCReadMin / 1000 + " µs\n");
		percent = formatter.format((txRemoteRead * 100.0) / txReads);
		stats.append("\tRemote reads " + txRemoteRead + " " + percent + "\n");
		stats.append("\t\tavg = " + txRReadAvg / 1000000 + " ms\n");
		stats.append("\t\tmax = " + txRReadMax / 1000000 + " ms\n");
		stats.append("\t\tmin = " + txRReadMin / 1000000 + " ms\n");
		percent = formatter.format((txLocalRead * 100.0) / txReads);
		stats.append("\tLocal reads " + txLocalRead + " " + percent + "\n");
		stats.append("\t\tavg = " + txLReadAvg / 1000 + " µs\n");
		stats.append("\t\tmax = " + txLReadMax / 1000 + " µs\n");
		stats.append("\t\tmin = " + txLReadMin / 1000 + " µs\n");
		stats.append("\tWaiting for doRead\n");
		stats.append("\t\tavg = " + waitingReadAvg / 1000 + " µs\n");
		stats.append("\t\tmax = " + waitingReadMax / 1000 + " µs\n");
		stats.append("\t\tmin = " + waitingReadMin / 1000 + " µs\n");
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
