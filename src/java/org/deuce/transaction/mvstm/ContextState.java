package org.deuce.transaction.mvstm;

import org.deuce.transaction.DistributedContextState;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class ContextState extends DistributedContextState
{
	private static final long serialVersionUID = 1L;
	final public int rv;
	public boolean readWriteHint;

	public ContextState(ReadSet rs, WriteSet ws, int ctxID, int atomicBlockId,
			int rv, boolean readWriteHint)
	{
		super(rs, ws, ctxID, atomicBlockId);
		this.rv = rv;
		this.readWriteHint = readWriteHint;
	}
}
