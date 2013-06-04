package org.deuce.distribution.replication.partial.protocol.score;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class VoteMsg implements Serializable
{
	private static final long serialVersionUID = 1L;
	public boolean outcome;
	public int proposedTimestamp;
	public int ctxID;
	public String trxID;

	public VoteMsg(boolean outcome, int proposedTimestamp, int ctxID,
			String trxID)
	{
		this.outcome = outcome;
		this.proposedTimestamp = proposedTimestamp;
		this.ctxID = ctxID;
		this.trxID = trxID;
	}
}
