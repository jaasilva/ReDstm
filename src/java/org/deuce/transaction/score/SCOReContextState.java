package org.deuce.transaction.score;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.GroupUtils;
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
	public SCOReContextState(ReadSet rs, WriteSet ws, int ctxID,
			int atomicBlockId, int sid, String trxID)
	{
		super(rs, ws, ctxID, atomicBlockId);
		this.sid = sid;
		this.trxID = trxID;
		this.origin = TribuDSTM.getLocalAddress();
	}

	/**
	 * @return
	 */
	public Group getInvolvedNodes()
	{
		Group group1 = ((SCOReReadSet) rs).getInvolvedNodes();
		Group group2 = ((SCOReWriteSet) ws).getInvolvedNodes();
		Group resGroup = GroupUtils.unionGroups(group1, group2);
		return resGroup;
	}
}
