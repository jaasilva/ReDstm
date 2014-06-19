package org.deuce.distribution.cache;

import org.deuce.distribution.replication.msgs.ControlMessage;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class CacheMsg extends ControlMessage
{
	private static final long serialVersionUID = 1L;
	public int validity;
	public int version;
	public int groupId;
	public iSetMsg piggyback = null;

	public CacheMsg()
	{
	}
}
