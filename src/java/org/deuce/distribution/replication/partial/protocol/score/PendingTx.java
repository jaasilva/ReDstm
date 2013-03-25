package org.deuce.distribution.replication.partial.protocol.score;

import org.deuce.distribution.groupcomm.Address;

/**
 * @author jaasilva
 * 
 */
public class PendingTx implements Comparable<PendingTx>
{
	public int ctxID;
	public Address addr;
	public int sid;

	/**
	 * @param ctxID
	 * @param sid
	 */
	public PendingTx(int ctxID, int sid, Address src)
	{
		this.ctxID = ctxID;
		this.addr = src;
		this.sid = sid;
	}

	@Override
	public int compareTo(PendingTx tx)
	{
		if (this.sid > tx.sid)
		{
			return 1;
		}
		else if (this.sid < tx.sid)
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
				&& (this.addr.equals(((PendingTx) obj).addr) && this.ctxID == ((PendingTx) obj).ctxID);
	}
}
