package org.deuce.transaction.score2.field;

import org.deuce.transaction.score2.Context;
import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>, jaasilva
 */
@ExcludeTM
public class VersionList
{
	public Version[] versions = new Version[Context.MAX_VERSIONS];
	public int curr = 0;

	public VersionList(int version, Object value)
	{
		set(version, value);
	}

	public Version set(int version, Object value)
	{
		int v = curr;
		Version ver = new Version(version, value, null);
		versions[v] = ver;

		curr = (curr + 1) & (Context.MAX_VERSIONS - 1);
		return ver;
	}

	public Version get(int version)
	{
		int v = (curr - 1) & (Context.MAX_VERSIONS - 1);
		do
		{
			Version ver = versions[v];
			if (ver.version <= version)
			{
				return ver;
			}
			v = (v - 1) & (Context.MAX_VERSIONS - 1);
		}
		while (v != curr);
		throw Context.VERSION_UNAVAILABLE_EXCEPTION;
	}

	public Version getLast()
	{
		return versions[(curr - 1) & (Context.MAX_VERSIONS - 1)];
	}

	public boolean isLast(int version)
	{
		return versions[(curr - 1) & (Context.MAX_VERSIONS - 1)].version == version;
	}

	public boolean isLast(Version ver)
	{
		return versions[(curr - 1) & (Context.MAX_VERSIONS - 1)] == ver;
	}
}
