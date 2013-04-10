package org.deuce.transaction.score.field;

import org.deuce.transaction.score.Context;
import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>, jaasilva
 */
@ExcludeTM
public class Version
{
	public volatile int version;
	public Version next;
	public int size;
	public Object value;

	public Version(int version, Object value, Version next)
	{
		this.version = version;
		this.next = next;
		this.value = value;
		this.size = next != null ? next.size + 1 : 1;
		if (size == Context.MAX_VERSIONS)
		{
			cleanVersions();
			size = Context.MAX_VERSIONS >>> 1; // divide by 2
		}
	}

	private void cleanVersions()
	{
		int c = Context.MAX_VERSIONS >>> 1; // divide by 2
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
		Version res = this;
		while (res.version > maxVersion)
		{
			res = res.next;
			if (res == null)
			{
				throw Context.VERSION_UNAVAILABLE_EXCEPTION;
			}
		}
		return res;
	}
}
