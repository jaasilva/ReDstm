package org.deuce.transaction.score;

import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.ReadSet;
import org.deuce.transaction.WriteSet;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReContextState extends DistributedContextState
{
	private static final long serialVersionUID = 399279683341037953L;
	public final int sid;

	/**
	 * @param rs
	 * @param ws
	 * @param ctxID
	 * @param atomicBlockId
	 * @param sid
	 */
	public SCOReContextState(ReadSet rs, WriteSet ws, int ctxID,
			int atomicBlockId, int sid)
	{
		super(rs, ws, ctxID, atomicBlockId);
		this.sid = sid;
	}
}
