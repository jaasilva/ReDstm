package org.deuce.distribution.replication.full.oid;

import org.deuce.distribution.replication.OID;
import org.deuce.distribution.replication.OIDFactory;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class UUIDFactory implements OIDFactory
{
	public OID generateOID()
	{
		return new UUID();
	}

	public OID generateOID(int oid)
	{
		return new UUID(oid);
	}
}
