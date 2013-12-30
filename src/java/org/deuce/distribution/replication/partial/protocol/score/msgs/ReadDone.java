package org.deuce.distribution.replication.partial.protocol.score.msgs;

import org.deuce.distribution.replication.msgs.ProtocolMessage;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class ReadDone extends ProtocolMessage
{
	private static final long serialVersionUID = 1L;
	public Object value;
	public int lastCommitted;
	public boolean mostRecent;

	public ReadDone(Object value, int lastCommitted, boolean mostRecent)
	{
		this.value = value;
		this.lastCommitted = lastCommitted;
		this.mostRecent = mostRecent;
	}
}
