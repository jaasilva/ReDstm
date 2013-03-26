package org.deuce.distribution.replication.partial.protocol.score;

/**
 * @author jaasilva
 * 
 */
public class DecideMessage
{
	public int ctxID;
	public int finalSid;
	public boolean result;

	/**
	 * @param ctxID
	 * @param finalSid
	 * @param result
	 */
	public DecideMessage(int ctxID, int finalSid, boolean result)
	{
		this.ctxID = ctxID;
		this.finalSid = finalSid;
		this.result = result;
	}
}
