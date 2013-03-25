package org.deuce.distribution.replication.partial.protocol.score;

import org.deuce.distribution.groupcomm.Address;

/**
 * @author jaasilva
 * 
 */
public class DecideMessage
{
	public int ctxID;
	public Address addr;
	public int finalSid;
	public boolean result;

	/**
	 * @param ctxID
	 * @param finalSid
	 * @param result
	 */
	public DecideMessage(int ctxID, int finalSid, boolean result, Address src)
	{
		this.ctxID = ctxID;
		this.addr = src;
		this.finalSid = finalSid;
		this.result = result;
	}
}
