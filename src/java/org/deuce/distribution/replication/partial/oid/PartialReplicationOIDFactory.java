package org.deuce.distribution.replication.partial.oid;

import org.deuce.distribution.replication.OIDFactory;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public interface PartialReplicationOIDFactory extends OIDFactory
{
	public PartialReplicationOID generateOID();

	public PartialReplicationOID generateFullReplicationOID();

	public PartialReplicationOID generateFullReplicationOID(int oid);

	public PartialReplicationOID generateOID(int oid);
}
