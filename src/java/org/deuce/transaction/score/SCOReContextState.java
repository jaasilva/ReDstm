package org.deuce.transaction.score;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReContextState extends DistributedContextState
{
	private static final long serialVersionUID = 399279683341037953L;
	public int sid;
	public String trxID;
	public Address origin;

	/**
	 * @param rs
	 * @param ws
	 * @param ctxID
	 * @param atomicBlockId
	 * @param sid
	 * @param trxID
	 */
	public SCOReContextState(SCOReReadSet rs, SCOReWriteSet ws, int ctxID,
			int atomicBlockId, int sid, String trxID)
	{
		super(rs, ws, ctxID, atomicBlockId);
		this.sid = sid;
		this.trxID = trxID;
		this.origin = TribuDSTM.getLocalAddress();
	}
}
