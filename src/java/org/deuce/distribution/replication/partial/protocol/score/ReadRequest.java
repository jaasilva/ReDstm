package org.deuce.distribution.replication.partial.protocol.score;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.groupcomm.Address;

/**
 * @author jaasilva
 * 
 */
public class ReadRequest
{
	public int ctxID;
	public Address addr;
	public ObjectMetadata metadata;
	public int readSid;
	public boolean firstRead;

	/**
	 * @param ctxID
	 * @param addr
	 * @param metadata
	 * @param readSid
	 * @param firstRead
	 */
	public ReadRequest(int ctxID, Address addr, ObjectMetadata metadata,
			int readSid, boolean firstRead)
	{
		this.ctxID = ctxID;
		this.addr = addr;
		this.metadata = metadata;
		this.readSid = readSid;
		this.firstRead = firstRead;
	}
}
