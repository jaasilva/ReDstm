package org.deuce.distribution.replication.partial.protocol.score;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class ReadRet implements Serializable
{
	private static final long serialVersionUID = 1L;
	public int ctxID;
	public int msgVersion;
	public ReadDone read;

	public ReadRet(int ctxID, int msgVersion, ReadDone read)
	{
		this.ctxID = ctxID;
		this.msgVersion = msgVersion;
		this.read = read;
	}
}
