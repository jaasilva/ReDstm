package org.deuce.transaction.score;

/**
 * @author jaasilva
 * 
 */
public class Version
{
	private Version next;
	private int size;
	private volatile int version;
	private Object value;

	/**
	 * 
	 */
	public Version(Object value, int version, Version next)
	{
		this.value = value;
		this.version = version;
		this.next = next;

		this.size = next != null ? next.size + 1 : 1;
		if (size == SCOReContext.MAX_VERSIONS)
		{
			cleanVersions();
			size = SCOReContext.MAX_VERSIONS >>> 1; // divide by 2
		}
	}

	/**
	 * 
	 */
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

	/**
	 * @param maxVersion
	 * @return
	 */
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

	/**
	 * @return
	 */
	public Object getValue()
	{
		return value;
	}
}
