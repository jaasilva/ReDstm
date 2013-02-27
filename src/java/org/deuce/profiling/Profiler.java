package org.deuce.profiling;

import java.util.LinkedList;
import java.util.List;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class Profiler
{
	static public boolean enabled = false;
	static protected Object lock = new Object();

	static protected List<Profiler> profilers = new LinkedList<Profiler>();

	static public void addProfiler(Profiler prof)
	{
		synchronized (profilers)
		{
			profilers.add(prof);
		}
	}

	/**
	 * Transaction throughput related.
	 */
	public long txCommitted, txSpecCommitted, txAborted, txSpecAborted,
			txLocal, txOutOfOrder;
	static public long txCommittedRemote, txSpecCommittedRemote,
			txAbortedRemote, txSpecAbortedRemote, txRemote, txOutOfOrderRemote;

	/**
	 * Incremental average.
	 */
	static protected long avgLocal, avgLocalNetTO, avgLocalNetURB, avgRemote,
			avgValidated, avgCommitted;

	/**
	 * Time related, in nanoseconds.
	 */
	protected long txAppStart, txAppEnd, txNetTOStart, txNetOptEnd, txNetTOEnd,
			txNetURBStart, txNetURBEnd, txValidateStart, txSpecValidateStart,
			txValidateEnd, txSpecValidateEnd, txCommitStart, txSpecCommitStart,
			txCommitEnd, txSpecCommitEnd, txSpecAbortStart, txSpecAbortEnd;
	protected static long txTimeAppAvg, txTimeAppMin = Long.MAX_VALUE,
			txTimeAppMax = Long.MIN_VALUE, txTimeNetTOAvg,
			txTimeNetTOMin = Long.MAX_VALUE, txTimeNetTOMax = Long.MIN_VALUE,
			txTimeNetURBAvg, txTimeNetURBMin = Long.MAX_VALUE,
			txTimeNetURBMax = Long.MIN_VALUE, txTimeNetOptAvg,
			txTimeNetOptMin = Long.MAX_VALUE, txTimeNetOptMax = Long.MIN_VALUE,
			txTimeValidateAvg, txTimeValidateMin = Long.MAX_VALUE,
			txTimeValidateMax = Long.MIN_VALUE, txTimeCommitAvg,
			txTimeCommitMin = Long.MAX_VALUE, txTimeCommitMax = Long.MIN_VALUE;

	/**
	 * Network related, in bytes.
	 */
	protected static long msgSent, msgSentSizeAvg,
			msgSentSizeMax = Long.MIN_VALUE, msgSentSizeMin = Long.MAX_VALUE,
			msgRecv, msgRecvSizeAvg, msgRecvSizeMax = Long.MIN_VALUE,
			msgRecvSizeMin = Long.MAX_VALUE;

	public boolean remote;

	public Profiler(boolean remote)
	{
		this.remote = remote;
		// if (remote)
		// txRemote++;
	}

	public void txCommitted()
	{
		if (enabled)
			// if (remote)
			// txCommittedRemote++;
			// else
			txCommitted++;
	}

	public void txSpecCommitted()
	{
		if (enabled)
			// if (remote)
			// txSpecCommittedRemote++;
			// else
			txSpecCommitted++;
	}

	public void txAborted()
	{
		if (enabled)
			// if (remote)
			// txAbortedRemote++;
			// else
			txAborted++;
	}

	public void txSpecAborted()
	{
		if (enabled)
			// if (remote)
			// txSpecAbortedRemote++;
			// else
			txSpecAborted++;
	}

	public void txOutOfOrder()
	{
		if (enabled)
			// if (remote)
			// txOutOfOrderRemote++;
			// else
			txOutOfOrder++;
	}

	public void onTxBegin()
	{
		if (enabled)
			txAppStart = System.nanoTime();
	}

	public void onTxAppCommit()
	{
		if (enabled)
		{
			txAppEnd = System.nanoTime();

			long appElapsed = txAppEnd - txAppStart;
			synchronized (lock)
			{
				if (appElapsed < txTimeAppMin)
					txTimeAppMin = appElapsed;
				if (appElapsed > txTimeAppMax)
					txTimeAppMax = appElapsed;
				txTimeAppAvg = incrementalAvg(txTimeAppAvg, appElapsed,
						avgLocal);

				avgLocal++;
			}
		}
	}

	public void onTOSend()
	{
		if (enabled)
			txNetTOStart = System.nanoTime();
	}

	public void onOptTODelivery()
	{
		if (enabled && !remote)
		{
			txNetOptEnd = System.nanoTime();

			long optElapsed = txNetOptEnd - txNetTOStart;
			synchronized (lock)
			{
				if (optElapsed < txTimeNetOptMin)
					txTimeNetOptMin = optElapsed;
				if (optElapsed > txTimeNetOptMax)
					txTimeNetOptMax = optElapsed;
				txTimeNetOptAvg = incrementalAvg(txTimeNetOptAvg, optElapsed,
						avgLocalNetTO);
			}
		}
	}

	public void onTODelivery()
	{
		if (enabled && !remote)
		{
			txNetTOEnd = System.nanoTime();

			long toElapsed = txNetTOEnd - txNetTOStart;
			synchronized (lock)
			{
				if (toElapsed < txTimeNetTOMin)
					txTimeNetTOMin = toElapsed;
				if (toElapsed > txTimeNetTOMax)
					txTimeNetTOMax = toElapsed;
				txTimeNetTOAvg = incrementalAvg(txTimeNetTOAvg, toElapsed,
						avgLocalNetTO);
				avgLocalNetTO++;
			}
		}
	}

	public void onURBSend()
	{
		if (enabled)
			txNetURBStart = System.nanoTime();
	}

	public void onURBDelivery()
	{
		if (enabled && !remote)
		{
			txNetURBEnd = System.nanoTime();

			long urbElapsed = txNetURBEnd - txNetURBStart;
			synchronized (lock)
			{
				if (urbElapsed < txTimeNetURBMin)
					txTimeNetURBMin = urbElapsed;
				if (urbElapsed > txTimeNetURBMax)
					txTimeNetURBMax = urbElapsed;
				txTimeNetURBAvg = incrementalAvg(txTimeNetURBAvg, urbElapsed,
						avgLocalNetURB);
				avgLocalNetURB++;
			}
		}
	}

	public void onTxValidateBegin()
	{
		if (enabled)
			txValidateStart = System.nanoTime();
	}

	public void onTxValidateEnd()
	{
		if (enabled)
		{
			txValidateEnd = System.nanoTime();

			long validateElapsed = txValidateEnd - txValidateStart;
			synchronized (lock)
			{
				if (validateElapsed < txTimeValidateMin)
					txTimeValidateMin = validateElapsed;
				if (validateElapsed > txTimeValidateMax)
					txTimeValidateMax = validateElapsed;
				txTimeValidateAvg = incrementalAvg(txTimeValidateAvg,
						validateElapsed, avgValidated);
				avgValidated++;
			}
		}
	}

	public void onTxSpecValidateBegin()
	{
		if (enabled)
			txSpecValidateStart = System.nanoTime();
	}

	public void onTxSpecValidateEnd()
	{
		if (enabled)
			txSpecValidateEnd = System.nanoTime();
	}

	public void onTxCommitStart()
	{
		if (enabled)
			txCommitStart = System.nanoTime();
	}

	public void onTxCommitEnd()
	{
		if (enabled)
		{
			txCommitEnd = System.nanoTime();
			long commitElapsed = txCommitEnd - txCommitStart;

			synchronized (lock)
			{
				if (commitElapsed < txTimeCommitMin)
					txTimeCommitMin = commitElapsed;
				if (commitElapsed > txTimeCommitMax)
					txTimeCommitMax = commitElapsed;

				txTimeCommitAvg = incrementalAvg(txTimeCommitAvg,
						commitElapsed, avgCommitted);
				avgCommitted++;
			}
		}
	}

	public void onTxSpecCommitStart()
	{
		if (enabled)
			txSpecCommitStart = System.nanoTime();
	}

	public void onTxSpecCommitEnd()
	{
		if (enabled)
			txSpecCommitEnd = System.nanoTime();
	}

	public void onTxSpecAbortStart()
	{
		if (enabled)
			txSpecAbortStart = System.nanoTime();
	}

	public void onTxSpecAbortEnd()
	{
		if (enabled)
			txSpecAbortEnd = System.nanoTime();
	}

	public void txProcessed(boolean committed)
	{
		if (enabled)
		{
			if (committed)
				txCommitted();
			else
				txAborted();
		}
	}

	public void newMsgSent(int bytes)
	{
		if (enabled)
		{
			msgSentSizeAvg = incrementalAvg(msgSentSizeAvg, bytes, msgSent);
			msgSent++;

			if (bytes < msgSentSizeMin)
				msgSentSizeMin = bytes;

			if (bytes > msgSentSizeMax)
				msgSentSizeMax = bytes;
		}
	}

	public void newMsgRecv(int bytes)
	{
		if (enabled)
		{
			msgRecvSizeAvg = incrementalAvg(msgRecvSizeAvg, bytes, msgRecv);
			msgRecv++;

			if (bytes < msgRecvSizeMin)
				msgRecvSizeMin = bytes;

			if (bytes > msgRecvSizeMax)
				msgRecvSizeMax = bytes;
		}
	}

	private static long incrementalAvg(long currentAvg, long value,
			long currentIteration)
	{
		return currentAvg + (value - currentAvg) / (currentIteration + 1);
	}

	public static void print()
	{
		long totalTxCommittedLocal = 0, totalTxAbortedLocal = 0, totalTxSpecCommittedLocal = 0, totalTxSpecAbortedLocal = 0, totalTxOutOfOrderLocal = 0, totalTxLocal = 0;
		long totalTxCommittedRemote = 0, totalTxAbortedRemote = 0, totalTxSpecCommittedRemote = 0, totalTxSpecAbortedRemote = 0, totalTxOutOfOrderRemote = 0;
		for (Profiler p : profilers)
		{
			if (!p.remote)
			{
				totalTxCommittedLocal += p.txCommitted;
				totalTxAbortedLocal += p.txAborted;
				totalTxSpecCommittedLocal += p.txSpecCommitted;
				totalTxSpecAbortedLocal += p.txSpecAborted;
				totalTxOutOfOrderLocal += p.txOutOfOrder;
				totalTxLocal += p.txLocal;
			}
			else
			{
				totalTxCommittedRemote += p.txCommitted;
				totalTxAbortedRemote += p.txAborted;
				totalTxSpecCommittedRemote += p.txSpecCommitted;
				totalTxSpecAbortedRemote += p.txSpecAborted;
				totalTxOutOfOrderRemote += p.txOutOfOrder;
			}
		}
		long totalTxCommitted = totalTxCommittedLocal + totalTxCommittedRemote;
		long totalTxAborted = totalTxAbortedLocal + totalTxAbortedRemote;
		long totalTxSpecCommitted = totalTxSpecCommittedLocal
				+ totalTxSpecCommittedRemote;
		long totalTxSpecAborted = totalTxSpecAbortedLocal
				+ totalTxSpecAbortedRemote;
		long totalTxOutOfOrder = totalTxOutOfOrderLocal
				+ totalTxOutOfOrderRemote;
		long totalTx = totalTxLocal + txRemote;

		System.out.println("\nSTATISTICS:");
		System.out
				.printf("  Committed   = %d	Local = %d	Remote = %d\n",
						totalTxCommitted, totalTxCommittedLocal,
						totalTxCommittedRemote);
		System.out.printf(
				"  Aborted     = %d (%.2f%%)	Local = %d	Remote = %d\n",
				totalTxAborted, 100.0 * totalTxAborted / totalTxCommitted,
				totalTxAbortedLocal, totalTxAbortedRemote);
		System.out.printf("  SCommitted  = %d\n", totalTxSpecCommitted);
		System.out.printf("  SAborted    = %d (%.2f%%)\n", totalTxSpecAborted,
				100.0 * totalTxSpecAborted / totalTx);
		System.out.printf("  Out of ordr = %d\n", totalTxOutOfOrder);
		System.out.printf("  Local       = %d (%.2f%%)\n",
				totalTxCommittedLocal, 100.0 * totalTxCommittedLocal
						/ totalTxCommitted);
		System.out.printf("  Remote      = %d (%.2f%%)\n",
				totalTxCommittedRemote, 100.0 * totalTxCommittedRemote
						/ totalTxCommitted);
		System.out.println();
		System.out.printf("  Tx execution time\n");
		System.out.printf("    Application\n");
		System.out.printf("      avg = %d µs\n", txTimeAppAvg / 1000);
		System.out.printf("      max = %d µs\n", txTimeAppMax / 1000);
		System.out.printf("      min = %d µs\n", txTimeAppMin / 1000);
		System.out.printf("    Network\n");
		System.out.printf("      TO\n");
		System.out.printf("        avg = %d ms\n", txTimeNetTOAvg / 1000000);
		System.out.printf("        max = %d ms\n", txTimeNetTOMax / 1000000);
		System.out.printf("        min = %d ms\n", txTimeNetTOMin / 1000000);
		System.out.printf("      Opt\n");
		System.out.printf("        avg = %d ms\n", txTimeNetOptAvg / 1000000);
		System.out.printf("        max = %d ms\n", txTimeNetOptMax / 1000000);
		System.out.printf("        min = %d ms\n", txTimeNetOptMin / 1000000);
		System.out.printf("      URB\n");
		System.out.printf("        avg = %d ms\n", txTimeNetURBAvg / 1000000);
		System.out.printf("        max = %d ms\n", txTimeNetURBMax / 1000000);
		System.out.printf("        min = %d ms\n", txTimeNetURBMin / 1000000);
		System.out.printf("    Validation\n");
		System.out.printf("      avg = %d µs\n", txTimeValidateAvg / 1000);
		System.out.printf("      max = %d µs\n", txTimeValidateMax / 1000);
		System.out.printf("      min = %d µs\n", txTimeValidateMin / 1000);
		System.out.printf("    Commit\n");
		System.out.printf("      avg = %d µs\n", txTimeCommitAvg / 1000);
		System.out.printf("      max = %d µs\n", txTimeCommitMax / 1000);
		System.out.printf("      min = %d µs\n", txTimeCommitMin / 1000);
		System.out.println();
		System.out.printf("  Msgs sent = %d\n", msgSent);
		System.out.printf("    avg     = %d bytes\n", msgSentSizeAvg);
		System.out.printf("    max     = %d bytes\n", msgSentSizeMax);
		System.out.printf("    min     = %d bytes\n", msgSentSizeMin);
		System.out.printf("  Msgs recv = %d\n", msgRecv);
		System.out.printf("    avg     = %d bytes\n", msgRecvSizeAvg);
		System.out.printf("    max     = %d bytes\n", msgRecvSizeMax);
		System.out.printf("    min     = %d bytes\n", msgRecvSizeMin);
	}
}
