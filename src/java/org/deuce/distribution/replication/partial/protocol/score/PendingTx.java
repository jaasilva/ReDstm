package org.deuce.distribution.replication.partial.protocol.score;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.score.SCOReContextState;

/**
 * @author jaasilva
 * 
 */
public class PendingTx implements Comparable<PendingTx>
{
	public DistributedContextState ctxState;
	public Address src;

	/**
	 * @param ctxID
	 * @param sid
	 */
	public PendingTx(DistributedContextState ctxState, Address src)
	{
		this.ctxState = ctxState;
		this.src = src;
	}

	@Override
	public int compareTo(PendingTx tx)
	{
		if (((SCOReContextState) this.ctxState).sid > ((SCOReContextState) tx.ctxState).sid)
		{
			return 1;
		}
		else if (((SCOReContextState) this.ctxState).sid < ((SCOReContextState) tx.ctxState).sid)
		{
			return -1;
		}
		else
		{
			return 0;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof PendingTx)
				&& (this.src.equals(((PendingTx) obj).src) && this.ctxState.ctxID == ((PendingTx) obj).ctxState.ctxID);
	}
}
