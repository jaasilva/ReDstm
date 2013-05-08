package org.deuce.distribution.replication.partial.protocol.score;

import java.io.Serializable;

/**
 * @author jaasilva
 * 
 */
public class ReadDone implements Serializable
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
