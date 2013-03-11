package org.deuce.distribution.replication.full.oid;

import org.deuce.distribution.replication.OID;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class SimpleOID implements OID
{
	private static final long serialVersionUID = 1L;

	private long localID;
	private int siteID; /* -1 for static IDs */

	protected SimpleOID(int siteID, long localID)
	{
		this.localID = localID;
		this.siteID = siteID;
	}

	protected SimpleOID(int localID)
	{
		this(-1, localID);
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof SimpleOID && ((SimpleOID) obj).localID == localID
				&& ((SimpleOID) obj).siteID == siteID;
	}

	@Override
	public int hashCode()
	{
		return (int) localID;
	}

	@Override
	public String toString()
	{
		// siteID:localID
		StringBuilder sb = new StringBuilder();
		sb.append(siteID);
		sb.append(':');
		sb.append(localID);
		return sb.toString();
	}
}
