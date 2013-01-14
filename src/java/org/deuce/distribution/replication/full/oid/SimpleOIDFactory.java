package org.deuce.distribution.replication.full.oid;

import java.util.concurrent.atomic.AtomicLong;

import org.deuce.distribution.replication.full.OID;
import org.deuce.distribution.replication.full.OIDFactory;
import org.deuce.transform.ExcludeTM;


@ExcludeTM
public class SimpleOIDFactory implements OIDFactory {
	public static final int SITE_ID = Integer.getInteger("tribu.site"); /* TODO Move this to a .properties 
	configuration file. */
//	private AtomicInteger localIDCounter = new AtomicInteger(0);
	private AtomicLong localIDCounter = new AtomicLong(0);
	
	public OID generateOID() {
		return new SimpleOID(SITE_ID, localIDCounter.incrementAndGet());
	}

	public OID generateOID(int oid) {
		return new SimpleOID(oid);
	}

}
