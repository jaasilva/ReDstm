package org.deuce.distribution.replication.partial.protocol.score;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class DecideMsg implements Serializable
{
	private static final long serialVersionUID = 1L;
	public int ctxID;
	public String trxID;
	public int finalSid;
	public boolean result;

	public DecideMsg(int ctxID, String trxID, int finalSid, boolean result)
	{
		this.ctxID = ctxID;
		this.trxID = trxID;
		this.finalSid = finalSid;
		this.result = result;
	}
}
