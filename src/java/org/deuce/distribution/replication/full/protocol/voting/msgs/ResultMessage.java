package org.deuce.distribution.replication.full.protocol.voting.msgs;

import org.deuce.distribution.replication.ProtocolMessage;
import org.deuce.transform.ExcludeTM;

/**
 * @author Tiago Vale
 */
@ExcludeTM
public class ResultMessage extends ProtocolMessage
{
	private static final long serialVersionUID = 4076576855178620723L;
	public int ctxID;
	public boolean result;

	public ResultMessage(int ctxID, boolean result)
	{
		this.ctxID = ctxID;
		this.result = result;
	}
}
