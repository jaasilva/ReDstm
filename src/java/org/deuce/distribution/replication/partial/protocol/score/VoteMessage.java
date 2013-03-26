package org.deuce.distribution.replication.partial.protocol.score;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class VoteMessage
{
	public int ctxID;
	public boolean result;
	public int proposedTimestamp;

	/**
	 * @param ctxID
	 * @param result
	 * @param proposedTimestamp
	 */
	public VoteMessage(int ctxID, boolean result, int proposedTimestamp)
	{
		this.ctxID = ctxID;
		this.result = result;
		this.proposedTimestamp = proposedTimestamp;
	}
}
