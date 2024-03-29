package org.deuce.distribution.replication.partial.protocol.score.msgs;

import org.deuce.distribution.replication.msgs.ProtocolMessage;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class VoteMsg extends ProtocolMessage
{
	private static final long serialVersionUID = 1L;
	public boolean outcome;
	public int proposedTimestamp;
	public int ctxID;
	public String txnID;

	public VoteMsg(boolean outcome, int proposedTimestamp, int ctxID,
			String trxID)
	{
		this.outcome = outcome;
		this.proposedTimestamp = proposedTimestamp;
		this.ctxID = ctxID;
		this.txnID = trxID;
	}
}
