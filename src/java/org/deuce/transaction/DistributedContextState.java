package org.deuce.transaction;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class DistributedContextState implements Serializable
{
	private static final long serialVersionUID = 1L;

	public Object rs;
	public Object ws;
	final public int ctxID;
	final public int atomicBlockId;

	public DistributedContextState(Object rs, Object ws, int ctxID,
			int atomicBlockId)
	{
		this.rs = rs;
		this.ws = ws;
		this.ctxID = ctxID;
		this.atomicBlockId = atomicBlockId;
	}
}
