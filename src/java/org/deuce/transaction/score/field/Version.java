package org.deuce.transaction.score.field;

import org.deuce.transaction.score.SCOReContext;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class Version
{
	public int version;
	public Version next;
	public Object value;

	public Version(int version, Object value, Version next)
	{
		this.version = version;
		this.next = next;
		this.value = value;
	}

	public Version get(int maxVersion)
	{
		Version res = this;
		while (res.version > maxVersion)
		{
			res = res.next;

			if (res == null)
			{
				throw SCOReContext.VERSION_UNAVAILABLE_EXCEPTION;
			}
		}
		return res;
	}

	public boolean equals(Object other)
	{ // CHECKME do I have to check the value?
		return other instanceof Version
				&& this.version == ((Version) other).version
		/* && this.value.equals(((Version) other).value) */;
	}
}
