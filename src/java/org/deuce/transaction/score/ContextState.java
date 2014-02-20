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
	public String trxID;
	public Address src; // XXX check (really needed?)

	public ContextState(ReadSet rs, WriteSet ws, int ctxID,
			int atomicBlockId, int sid, String trxID)
	{
		super(rs, ws, ctxID, atomicBlockId);
		this.sid = sid;
		this.trxID = trxID;
		this.src = TribuDSTM.getLocalAddress();
	}
}