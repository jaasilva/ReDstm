package org.deuce.distribution.replication.partial.protocol.score;

/**
 * @author jaasilva
 * 
 */
public class DecideMessage
{
	public int ctxID;
	public String trxID;
	public int finalSid;
	public boolean result;

	/**
	 * @param ctxID
	 * @param finalSid
	 * @param result
	 */
	public DecideMessage(int ctxID, String trxID, int finalSid, boolean result)
	{
		this.ctxID = ctxID;
		this.trxID = trxID;
		this.finalSid = finalSid;
		this.result = result;
	}
}
