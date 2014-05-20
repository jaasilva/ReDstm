package org.deuce.profiling;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class Profiler
{
	private static boolean ENABLED = false;
	private static final int THREADS = 16;

	/**
	 * Transaction throughput related.
	 */
	private static long txCommitted = 0, txAborted = 0, txTimeout = 0;

	/**
	 * Incremental average.
	 */
	private static long txVotesIt = 0, txValidateIt = 0, txCommitIt = 0,
			msgSent = 0, msgRecv = 0, txRReadIt = 0, txLReadIt = 0, serIt = 0,
			waitingReadIt = 0, distCommitIt = 0, confirmationIt = 0,
			remoteReadMsgIt = 0;

	/**
	 * Time related, in nanoseconds.
	 */
	private static long[] txVotes = new long[THREADS];
	private static long[] txValidate = new long[THREADS];
	private static long[] txCommit = new long[THREADS];
	private static long[] txRRead = new long[THREADS];
	private static long[] txLRead = new long[THREADS];
	private static long[] confirmation = new long[THREADS];
	private static long txVotesAvg = 0, txVotesMax = Long.MIN_VALUE,
			txVotesMin = Long.MAX_VALUE, txValidateAvg = 0,
			txValidateMax = Long.MIN_VALUE, txValidateMin = Long.MAX_VALUE,
			txCommitAvg = 0, txCommitMax = Long.MIN_VALUE,
			txCommitMin = Long.MAX_VALUE, txRReadAvg = 0,
			txRReadMax = Long.MIN_VALUE, txRReadMin = Long.MAX_VALUE,
			txLReadAvg = 0, txLReadMax = Long.MIN_VALUE,
			txLReadMin = Long.MAX_VALUE, serAvg = 0, serMax = Long.MIN_VALUE,
			serMin = Long.MAX_VALUE, waitingReadAvg = 0,
			waitingReadMax = Long.MIN_VALUE, waitingReadMin = Long.MAX_VALUE,
			txLocalRead = 0, txRemoteRead = 0, txReads = 0, txWsReads = 0,
			totalCommit = 0, confirmationMax = Long.MIN_VALUE,
			confirmationMin = Long.MAX_VALUE, confirmationAvg = 0,
			cacheTry = 0, cacheHit = 0, remoteReadMsgSizeAvg = 0,
			remoteMsgSizeMin = Long.MAX_VALUE,
			remoteMsgSizeMax = Long.MIN_VALUE, remoteReadOk = 0,
			cacheNoKey = 0, cacheNoVisibleVersion = 0, cacheNoValidVersion = 0;

	/**
	 * Network related, in bytes.
	 */
	private static long msgSentSizeAvg = 0, msgSentSizeMax = Long.MIN_VALUE,
			msgSentSizeMin = Long.MAX_VALUE, msgRecvSizeAvg = 0,
			msgRecvSizeMax = Long.MIN_VALUE, msgRecvSizeMin = Long.MAX_VALUE;

	public static void enable()
	{
		ENABLED = true;
	}

	public static void disable()
	{
		ENABLED = false;
	}

	private static long incAvg(long currAvg, long value, long currIt)
	{
		return currAvg + ((value - currAvg) / (currIt + 1));
	}

	private static final Object lock1 = new Object();

	public static void txCommitted()
	{
		if (ENABLED)
		{
			synchronized (lock1)
			{
				txCommitted++;
			}
		}
	}

	private static final Object lock2 = new Object();

	public static void txAborted()
	{
		if (ENABLED)
		{
			synchronized (lock2)
			{
				txAborted++;
			}
		}
	}

	public static void txProcessed(boolean committed)
	{
		if (ENABLED)
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

	private static final Object lock3 = new Object();

	public static void txTimeout()
	{
		if (ENABLED)
		{
			synchronized (lock3)
			{
				txTimeout++;
			}
		}
	}

	public static void onTxValidateBegin(int ctxID)
	{
		if (ENABLED)
		{
			long txValidateStart = System.nanoTime();
			txValidate[ctxID] = txValidateStart;
		}
	}

	private static final Object lock5 = new Object();

	public static void onTxValidateFinish(int ctxID)
	{
		if (ENABLED)
		{
			long txValidateEnd = System.nanoTime();
			long elapsed = txValidateEnd - txValidate[ctxID];

			if (elapsed != txValidateEnd && elapsed > 0)
			{
				synchronized (lock5)
				{
					if (elapsed < txValidateMin)
					{
						txValidateMin = elapsed;
					}
					if (elapsed > txValidateMax)
					{
						txValidateMax = elapsed;
					}
					txValidateAvg = incAvg(txValidateAvg, elapsed,
							txValidateIt++);
				}
			}
			txValidate[ctxID] = 0;
		}
	}

	public static void onTxCommitBegin(int ctxID)
	{
		if (ENABLED)
		{
			long txCommitStart = System.nanoTime();
			txCommit[ctxID] = txCommitStart;
		}
	}

	private static final Object lock6 = new Object();

	public static void onTxCommitFinish(int ctxID)
	{
		if (ENABLED)
		{
			long txCommitEnd = System.nanoTime();
			long elapsed = txCommitEnd - txCommit[ctxID];

			if (elapsed != txCommitEnd && elapsed > 0)
			{
				synchronized (lock6)
				{
					if (elapsed < txCommitMin)
					{
						txCommitMin = elapsed;
					}
					if (elapsed > txCommitMax)
					{
						txCommitMax = elapsed;
					}
					txCommitAvg = incAvg(txCommitAvg, elapsed, txCommitIt++);
				}
			}
			txCommit[ctxID] = 0;
		}
	}

	private static final Object lock8 = new Object();

	public static void onTxRemoteReadBegin(int ctxID)
	{
		if (ENABLED)
		{
			long txReadStart = System.nanoTime();
			txRRead[ctxID] = txReadStart;
			synchronized (lock8)
			{
				txReads++;
				txRemoteRead++;
			}
		}
	}

	private static final Object lock9 = new Object();

	public static void onTxRemoteReadFinish(int ctxID)
	{
		if (ENABLED)
		{
			long txReadEnd = System.nanoTime();
			long elapsed = txReadEnd - txRRead[ctxID];

			if (elapsed != txReadEnd && elapsed > 0)
			{
				synchronized (lock9)
				{
					if (elapsed < txRReadMin)
					{
						txRReadMin = elapsed;
					}
					if (elapsed > txRReadMax)
					{
						txRReadMax = elapsed;
					}
					txRReadAvg = incAvg(txRReadAvg, elapsed, txRReadIt++);
				}
			}
			txRRead[ctxID] = 0;
		}
	}

	public static void onTxLocalReadBegin(int ctxID)
	{
		if (ENABLED)
		{
			long txReadStart = System.nanoTime();
			txLRead[ctxID] = txReadStart;
			synchronized (lock8)
			{
				txReads++;
				txLocalRead++;
			}
		}
	}

	private static final Object lock11 = new Object();

	public static void onTxLocalReadFinish(int ctxID)
	{
		if (ENABLED)
		{
			long txReadEnd = System.nanoTime();
			long elapsed = txReadEnd - txLRead[ctxID];

			if (elapsed != txReadEnd && elapsed > 0)
			{
				synchronized (lock11)
				{
					if (elapsed < txLReadMin)
					{
						txLReadMin = elapsed;
					}
					if (elapsed > txLReadMax)
					{
						txLReadMax = elapsed;
					}
					txLReadAvg = incAvg(txLReadAvg, elapsed, txLReadIt++);
				}
			}
			txLRead[ctxID] = 0;
		}
	}

	public static void onTxWsRead(int ctxID)
	{
		if (ENABLED)
		{
			synchronized (lock8)
			{
				txReads++;
				txWsReads++;
			}
		}
	}

	private static final Object lock15 = new Object();

	public static void onWaitingRead(long time)
	{
		if (ENABLED)
		{
			if (time > 0)
			{
				synchronized (lock15)
				{
					if (time < waitingReadMin)
					{
						waitingReadMin = time;
					}
					if (time > waitingReadMax)
					{
						waitingReadMax = time;
					}
					waitingReadAvg = incAvg(waitingReadAvg, time,
							waitingReadIt++);
				}
			}
		}
	}

	private static final Object lock16 = new Object();

	public static void onSerializationFinish(long elapsed)
	{
		if (ENABLED)
		{
			synchronized (lock16)
			{
				if (elapsed < serMin)
				{
					serMin = elapsed;
				}
				if (elapsed > serMax)
				{
					serMax = elapsed;
				}
				serAvg = incAvg(serAvg, elapsed, serIt++);
			}
		}
	}

	public static void onPrepSend(int ctxID)
	{
		if (ENABLED)
		{
			long txPrepStart = System.nanoTime();
			txVotes[ctxID] = txPrepStart;
		}
	}

	private static final Object lock17 = new Object();

	public static void onLastVoteReceived(int ctxID)
	{
		if (ENABLED)
		{
			long txPrepEnd = System.nanoTime();
			long elapsed = txPrepEnd - txVotes[ctxID];

			if (elapsed != txPrepEnd && elapsed > 0)
			{
				synchronized (lock17)
				{
					if (elapsed < txVotesMin)
					{
						txVotesMin = elapsed;
					}
					if (elapsed > txVotesMax)
					{
						txVotesMax = elapsed;
					}
					txVotesAvg = incAvg(txVotesAvg, elapsed, txVotesIt++);
				}
			}
			txVotes[ctxID] = 0;
		}
	}

	private static final Object lock18 = new Object();

	public static void newMsgSent(int bytes)
	{
		if (ENABLED)
		{
			synchronized (lock18)
			{
				if (bytes < msgSentSizeMin)
				{
					msgSentSizeMin = bytes;
				}
				if (bytes > msgSentSizeMax)
				{
					msgSentSizeMax = bytes;
				}
				msgSentSizeAvg = incAvg(msgSentSizeAvg, bytes, msgSent++);
			}
		}
	}

	private static final Object lock19 = new Object();

	public static void newMsgRecv(int bytes)
	{
		if (ENABLED)
		{
			synchronized (lock19)
			{
				if (bytes < msgRecvSizeMin)
				{
					msgRecvSizeMin = bytes;
				}
				if (bytes > msgRecvSizeMax)
				{
					msgRecvSizeMax = bytes;
				}
				msgRecvSizeAvg = incAvg(msgRecvSizeAvg, bytes, msgRecv++);
			}
		}
	}

	private static final Object lock21 = new Object();
	private static final int REPLICAS = Integer.getInteger("tribu.replicas");

	public static void whatNodes(int nodes)
	{
		if (ENABLED)
		{
			if (nodes == REPLICAS)
			{
				synchronized (lock21)
				{
					totalCommit++;
				}
			}
		}
	}

	public static void onTxConfirmationBegin(int ctxID)
	{
		if (ENABLED)
		{
			long txConfStart = System.nanoTime();
			confirmation[ctxID] = txConfStart;
		}
	}

	private static final Object lock22 = new Object();

	public static void onTxConfirmationFinish(int ctxID)
	{
		if (ENABLED)
		{
			long txConfEnd = System.nanoTime();
			long elapsed = txConfEnd - confirmation[ctxID];

			if (elapsed != txConfEnd && elapsed > 0)
			{
				synchronized (lock22)
				{
					if (elapsed < confirmationMin)
					{
						confirmationMin = elapsed;
					}
					if (elapsed > confirmationMax)
					{
						confirmationMax = elapsed;
					}
					confirmationAvg = incAvg(confirmationAvg, elapsed,
							confirmationIt++);
				}
			}
			confirmation[ctxID] = 0;
		}
	}

	private static final Object lock23 = new Object();

	public static void onCacheTry()
	{
		if (ENABLED)
		{
			synchronized (lock23)
			{
				cacheTry++;
			}
		}
	}

	private static final Object lock24 = new Object();

	public static void onCacheHit()
	{
		if (ENABLED)
		{
			synchronized (lock24)
			{
				cacheHit++;
			}
		}
	}

	private static final Object lock25 = new Object();

	public static void newRemoteReadRecv(int bytes)
	{
		if (ENABLED)
		{
			synchronized (lock25)
			{
				if (bytes < remoteMsgSizeMin)
				{
					remoteMsgSizeMin = bytes;
				}
				if (bytes > remoteMsgSizeMax)
				{
					remoteMsgSizeMax = bytes;
				}
				remoteReadMsgSizeAvg = incAvg(remoteReadMsgSizeAvg, bytes,
						remoteReadMsgIt++);
			}
		}
	}

	private static final Object lock26 = new Object();

	public static void remoteReadOk()
	{
		if (ENABLED)
		{
			synchronized (lock26)
			{
				remoteReadOk++;
			}
		}
	}

	private static final Object lock27 = new Object();

	public static void onCacheNoKey()
	{
		if (ENABLED)
		{
			synchronized (lock27)
			{
				cacheNoKey++;
			}
		}
	}

	private static final Object lock28 = new Object();

	public static void onCacheNoVisibleVersion()
	{
		if (ENABLED)
		{
			synchronized (lock28)
			{
				cacheNoVisibleVersion++;
			}
		}
	}

	private static final Object lock29 = new Object();

	public static void onCacheNoValidVersion()
	{
		if (ENABLED)
		{
			synchronized (lock29)
			{
				cacheNoValidVersion++;
			}
		}
	}

	public static void print()
	{
		StringBuffer stats = new StringBuffer();
		NumberFormat formatter = new DecimalFormat("#.#####");
		String str = "";

		stats.append("\n################################################\n");
		stats.append("################## STATISTICS ##################\n");
		stats.append("################################################\n");

		stats.append("=== Throughput\n");

		stats.append("  Committed = " + txCommitted + "\n");
		str = formatter.format((txAborted * 100.0) / txCommitted);
		stats.append("  Aborted   = " + txAborted + " (" + str + ")\n");
		str = formatter.format((txTimeout * 100.0) / txCommitted);
		stats.append("  Timetout  = " + txTimeout + " (" + str + ")\n");

		stats.append("=== Transactions\n");

		stats.append("  Validation Operation\n");
		str = formatter.format(txValidateAvg / 1000.0);
		stats.append("    avg = " + str + " mms\n");
		str = formatter.format(txValidateMax / 1000.0);
		stats.append("    max = " + str + " mms\n");
		str = formatter.format(txValidateMin / 1000.0);
		stats.append("    min = " + str + " mms\n");

		stats.append("  Commit Operation\n");
		str = formatter.format(txCommitAvg / 1000.0);
		stats.append("    avg = " + str + " mms\n");
		str = formatter.format(txCommitMax / 1000.0);
		stats.append("    max = " + str + " mms\n");
		str = formatter.format(txCommitMin / 1000.0);
		stats.append("    min = " + str + " mms\n");

		stats.append("  Distributed Commit\n");
		stats.append("    Total = " + distCommitIt + "\n");
		str = formatter.format((totalCommit * 100.0) / distCommitIt);
		stats.append("    Full  = " + totalCommit + " (" + str + ")\n");

		str = formatter.format(confirmationAvg / 1000000.0);
		stats.append("    avg = " + str + " ms\n");
		str = formatter.format(confirmationMax / 1000000.0);
		stats.append("    max = " + str + " ms\n");
		str = formatter.format(confirmationMin / 1000000.0);
		stats.append("    min = " + str + " ms\n");

		boolean correct = (txReads == (txWsReads + txRemoteRead + txLocalRead));
		stats.append("  Reads " + txReads + " (" + correct + ")\n");

		str = formatter.format((txLocalRead * 100.0) / txReads);
		stats.append("    Local  = " + txLocalRead + " (" + str + ")\n");
		str = formatter.format((txWsReads * 100.0) / txReads);
		stats.append("    WS     = " + txWsReads + " (" + str + ")\n");
		str = formatter.format((txRemoteRead * 100.0) / txReads);
		stats.append("    Remote = " + txRemoteRead + " (" + str + ")\n");

		stats.append("    --Local Reads\n");
		str = formatter.format(txLReadAvg / 1000.0);
		stats.append("      avg = " + str + " mms\n");
		str = formatter.format(txLReadMax / 1000.0);
		stats.append("      max = " + str + " mms\n");
		str = formatter.format(txLReadMin / 1000.0);
		stats.append("      min = " + str + " mms\n");

		stats.append("    --Remote Reads\n");
		str = formatter.format(txRReadAvg / 1000000.0);
		stats.append("      avg = " + str + " ms\n");
		str = formatter.format(txRReadMax / 1000000.0);
		stats.append("      max = " + str + " ms\n");
		str = formatter.format(txRReadMin / 1000000.0);
		stats.append("      min = " + str + " ms\n");

		str = formatter.format((cacheHit * 100.0) / cacheTry);
		stats.append("    --Cache Hits " + cacheHit + "/" + cacheTry + " ("
				+ str + ")\n");
		stats.append("      no key = " + cacheNoKey + "\n");
		stats.append("      no vis ver = " + cacheNoVisibleVersion + "\n");
		stats.append("      no val ver = " + cacheNoValidVersion + "\n");

		stats.append("  Waiting for doRead\n");
		str = formatter.format(waitingReadAvg / 1000.0);
		stats.append("    avg = " + str + " mms\n");
		str = formatter.format(waitingReadMax / 1000.0);
		stats.append("    max = " + str + " mms\n");
		str = formatter.format(waitingReadMin / 1000.0);
		stats.append("    min = " + str + " mms\n");

		stats.append("  Serialization\n");
		str = formatter.format(serAvg / 1000000.0);
		stats.append("    avg = " + str + " ms\n");
		str = formatter.format(serMax / 1000000.0);
		stats.append("    max = " + str + " ms\n");
		str = formatter.format(serMin / 1000000.0);
		stats.append("    min = " + str + " ms\n");

		stats.append("=== Network\n");

		stats.append("  Waiting for Votes\n");
		str = formatter.format(txVotesAvg / 1000000.0);
		stats.append("    avg = " + str + " ms\n");
		str = formatter.format(txVotesMax / 1000000.0);
		stats.append("    max = " + str + " ms\n");
		str = formatter.format(txVotesMin / 1000000.0);
		stats.append("    min = " + str + " ms\n");

		stats.append("  Msgs Sent = " + msgSent + "\n");
		stats.append("    avg = " + msgSentSizeAvg + " bytes\n");
		stats.append("    max = " + msgSentSizeMax + " bytes\n");
		stats.append("    min = " + msgSentSizeMin + " bytes\n");

		stats.append("  Msgs Recv = " + msgRecv + "\n");
		stats.append("    avg = " + msgRecvSizeAvg + " bytes\n");
		stats.append("    max = " + msgRecvSizeMax + " bytes\n");
		stats.append("    min = " + msgRecvSizeMin + " bytes\n");

		str = formatter.format((remoteReadOk * 100.0) / remoteReadMsgIt);
		stats.append("  Remote Read Msgs Recv = " + remoteReadOk + "/"
				+ remoteReadMsgIt + " (" + str + ")\n");
		stats.append("    avg = " + remoteReadMsgSizeAvg + " bytes\n");
		stats.append("    max = " + remoteMsgSizeMax + " bytes\n");
		stats.append("    min = " + remoteMsgSizeMin + " bytes\n");

		System.out.println(stats.toString());
	}
}
