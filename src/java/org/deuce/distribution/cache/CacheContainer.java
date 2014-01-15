package org.deuce.distribution.cache;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class CacheContainer implements Comparable<CacheContainer>, Serializable
{
	private static final long serialVersionUID = 1L;
	public Object value;
	public int version;
	public Validity validity;

	@Override
	public int compareTo(CacheContainer obj)
	{
		return obj.version - this.version;
	}
}
