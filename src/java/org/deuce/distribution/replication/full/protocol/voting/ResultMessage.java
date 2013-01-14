package org.deuce.distribution.replication.full.protocol.voting;

import org.deuce.transform.ExcludeTM;

/**
 * FIXME: Refactor.
 * 
 * @author Tiago Vale
 */
@ExcludeTM
public class ResultMessage implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	public int ctxID;
	public boolean result;

	public ResultMessage(int ctxID, boolean result) {
		this.ctxID = ctxID;
		this.result = result;
	}
}
