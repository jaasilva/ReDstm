package org.deuce.distribution.replication.partial.protocol.score.msgs;

import java.io.Serializable;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class ReadReq implements Serializable
{
	private static final long serialVersionUID = 1L;
	public int ctxID;
	public ObjectMetadata metadata;
	public int readSid;
	public boolean firstRead;
	public int msgVersion;

	public ReadReq(int ctxID, ObjectMetadata metadata, int readSid,
			boolean firstRead, int msgVersion)
	{
		this.ctxID = ctxID;
		this.metadata = metadata;
		this.readSid = readSid;
		this.firstRead = firstRead;
		this.msgVersion = msgVersion;
	}
}
