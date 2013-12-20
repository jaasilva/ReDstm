package org.deuce.transaction;

import org.deuce.distribution.replication.ProtocolMessage;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class DistributedContextState extends ProtocolMessage
{
	private static final long serialVersionUID = 1L;
	public Object rs;
	public Object ws;
	public int ctxID;
	public int atomicBlockId;

	public DistributedContextState(Object rs, Object ws, int ctxID,
			int atomicBlockId)
	{
		this.rs = rs;
		this.ws = ws;
		this.ctxID = ctxID;
		this.atomicBlockId = atomicBlockId;
	}
}
