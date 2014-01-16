package org.deuce.transaction.score.field;

import java.io.Serializable;

import org.deuce.transaction.score.SCOReContext;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class Version implements Serializable
{
	private static final long serialVersionUID = 1L;
	public int version;
	public Version next;
	public Object value;
	private int size;
	public int validity = -1;

	public Version(int version, Object value, Version next)
	{
		this.version = version;
		this.next = next;
		this.value = value;
		this.size = next != null ? next.size + 1 : 0; // XXX I think it is 0.
		if (size == SCOReContext.MAX_VERSIONS)
		{
			cleanVersions();
			size = SCOReContext.MAX_VERSIONS >>> 1; // divide by 2
		}
	}

	private void cleanVersions()
	{
		int c = SCOReContext.MAX_VERSIONS >>> 1; // divide by 2
		Version v = this;
		while (c > 1)
		{
			v = v.next;
			c--;
		}
		v.next = null;
	}

	public Version get(int maxVersion)
	{
		Version prev = null;
		Version res = this;
		while (res.version > maxVersion)
		{
			prev = res;
			res = res.next;
			if (res == null)
			{
				throw SCOReContext.VERSION_UNAVAILABLE_EXCEPTION;
			}
		}
		res.validity = (prev != null) ? prev.version : -1; // XXX cache
		return res;
	}

	public boolean equals(Object other)
	{
		return other instanceof Version
				&& this.version == ((Version) other).version;
	}
}
