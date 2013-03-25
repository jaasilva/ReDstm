package org.deuce.distribution.replication.partial.protocol.score;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class VoteMessage
{
	public int ctxID;
	public Address addr;
	public boolean result;
	public int proposedTimestamp;

	/**
	 * @param ctxID
	 * @param result
	 * @param proposedTimestamp
	 */
	public VoteMessage(int ctxID, boolean result, int proposedTimestamp, Address src)
	{
		this.ctxID = ctxID;
		this.addr = src;
		this.result = result;
		this.proposedTimestamp = proposedTimestamp;
	}
}
