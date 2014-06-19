package org.deuce.transaction.score.field;

import java.io.Serializable;

import org.deuce.distribution.Defaults;
import org.deuce.transaction.TransactionException;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class Version implements Serializable
{
	private static final long serialVersionUID = 1L;
	public static final TransactionException VERSION_UNAVAILABLE_EXCEPTION = new TransactionException(
			"Fail on retrieveing an older or unexistent version.");
	public static final int MAX_VERSIONS = Integer
			.getInteger(Defaults._SCORE_MAX_VERSIONS,
					Defaults.SCORE_MAX_VERSIONS);

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
		this.size = next != null ? next.size + 1 : 0;
		if (size == MAX_VERSIONS)
		{
			cleanVersions();
			size = MAX_VERSIONS >>> 1; // divide by 2
		}
	}

	private void cleanVersions()
	{
		int c = MAX_VERSIONS >>> 1; // divide by 2
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
				throw VERSION_UNAVAILABLE_EXCEPTION;
			}
		}
		res.validity = prev != null ? prev.version : -1; // chache stuff
		return res;
	}

	@Override
	public boolean equals(Object other)
	{
		return other instanceof Version
				&& this.version == ((Version) other).version;
	}
}
