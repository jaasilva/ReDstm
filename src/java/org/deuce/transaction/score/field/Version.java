package org.deuce.transaction.score.field;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.deuce.transaction.score.SCOReContext;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class Version implements Serializable
{
	private static final Logger LOGGER = Logger.getLogger(Version.class);
	private static final long serialVersionUID = 1L;
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
		LOGGER.trace("- " + res.version);
		return res;
	}

	public boolean equals(Object other)
	{
		return other instanceof Version
				&& this.version == ((Version) other).version
		/* && this.value.equals(((Version) other).value) */;
	}
}
