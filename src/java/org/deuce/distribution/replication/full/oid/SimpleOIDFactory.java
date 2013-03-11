package org.deuce.distribution.replication.full.oid;

import java.util.concurrent.atomic.AtomicLong;

import org.deuce.distribution.replication.OID;
import org.deuce.distribution.replication.OIDFactory;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class SimpleOIDFactory implements OIDFactory
{
	public static final int SITE_ID = Integer.getInteger("tribu.site");
	private AtomicLong localIDCounter = new AtomicLong(0);

	public OID generateOID()
	{
		return new SimpleOID(SITE_ID, localIDCounter.incrementAndGet());
	}

	public OID generateOID(int oid)
	{
		return new SimpleOID(oid);
	}

}
