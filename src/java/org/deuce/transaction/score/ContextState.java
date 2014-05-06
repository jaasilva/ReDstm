package org.deuce.transaction.score;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.transaction.DistributedContextState;

/**
 * @author jaasilva
 */
public class ContextState extends DistributedContextState
{
	private static final long serialVersionUID = 1L;
	public int sid;
	public String txnID;
	public Address src; // CHECKME really needed?

	public ContextState(ReadSet rs, WriteSet ws, int ctxID, int atomicBlockId,
			int sid, String trxID)
	{
		super(rs, ws, ctxID, atomicBlockId);
		this.sid = sid;
		this.txnID = trxID;
		this.src = TribuDSTM.getLocalAddress();
	}
}
