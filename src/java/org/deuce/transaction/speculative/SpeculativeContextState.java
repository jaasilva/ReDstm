package org.deuce.transaction.speculative;

import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.ReadSet;
import org.deuce.transaction.WriteSet;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class SpeculativeContextState extends DistributedContextState
{
	private static final long serialVersionUID = 1L;

	public int speculativeVersionNumber;

	public SpeculativeContextState(ReadSet rs, WriteSet ws, int ctxID,
			int atomicBlockId, int speculativeVersionNumber)
	{
		super(rs, ws, ctxID, atomicBlockId);
		this.speculativeVersionNumber = speculativeVersionNumber;
	}
}
