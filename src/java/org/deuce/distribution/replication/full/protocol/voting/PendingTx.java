package org.deuce.distribution.replication.full.protocol.voting;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class PendingTx {
	public static final int WAITING = 0;
	public static final int VALIDATED = 1;
	public static final int COMMITTED = 2;
	public static final int ABORTED = 3;

	public Address src;
	public DistributedContextState ctxState;
	public int result = WAITING;

	public PendingTx(Address src, DistributedContextState msg) {
		this.src = src;
		this.ctxState = msg;
	}
}
