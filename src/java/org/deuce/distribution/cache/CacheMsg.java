package org.deuce.distribution.cache;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class CacheMsg implements Serializable
{
	private static final long serialVersionUID = 1L;
	public int validity;
	public int version;
	public int groupId; // XXX check. I think I dont need this

	public CacheMsg()
	{
	}
}
