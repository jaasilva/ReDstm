package org.deuce.distribution.replication.full.protocol.voting.msgs;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.msgs.ProtocolMessage;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class PendingTx extends ProtocolMessage
{
	private static final long serialVersionUID = 1L;
	public static final int WAITING = 0;
	public static final int VALIDATED = 1;
	public static final int COMMITTED = 2;
	public static final int ABORTED = 3;

	public Address src;
	public DistributedContextState ctxState;
	public int result = WAITING;

	public PendingTx(Address src, DistributedContextState msg)
	{
		this.src = src;
		this.ctxState = msg;
	}
}
