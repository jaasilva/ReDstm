package org.deuce.transaction.tl2;

import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.ReadSet;
import org.deuce.transaction.WriteSet;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class ContextState extends DistributedContextState
{
	private static final long serialVersionUID = 1L;
	final public int rv;

	public ContextState(ReadSet rs, WriteSet ws, int ctxID, int atomicBlockId,
			int rv)
	{
		super(rs, ws, ctxID, atomicBlockId);
		this.rv = rv;
	}

}
