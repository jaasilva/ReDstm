package org.deuce.distribution.replication.partial.protocol.score;

import org.deuce.distribution.groupcomm.Address;

/**
 * @author jaasilva
 * 
 */
public class ReadReturn
{
	public int ctxID;
	public Address addr;
	public Object value;
	public int mostRecent;
	public int lastCommitted;

	public ReadReturn(int ctxID, Address addr, Object value, int mostRecent,
			int lastCommitted)
	{
		this.ctxID = ctxID;
		this.addr = addr;
		this.value = value;
		this.mostRecent = mostRecent;
		this.lastCommitted = lastCommitted;
	}
}
