package org.deuce.distribution.replication.partial;

import org.deuce.distribution.replication.OIDFactory;
import org.deuce.transform.ExcludeTM;

/**
 * This factory class constructs new partial replication distribution metadata.
 * 
 * @author jaasilva
 */
@ExcludeTM
public interface PartialReplicationOIDFactory extends OIDFactory
{
	/**
	 * Creates a new PRepMetadata with id = rand(), group = ALL, partialGroup =
	 * [].
	 * 
	 * @return the newly created metadata.
	 */
	public PartialReplicationOID generateOID();

	/**
	 * Creates a new PRepMetadata with id = rand(), group = partialGroup = ALL.
	 * 
	 * @return the newly created metadata.
	 */
	public PartialReplicationOID generateFullReplicationOID();

	/**
	 * Creates a new PRepMetadata with id = rand(oid), group = partialGroup =
	 * ALL.
	 * 
	 * @param oid - the seed for the id creation.
	 * @return the newly created metadata.
	 */
	public PartialReplicationOID generateFullReplicationOID(int oid);

	/**
	 * Creates a new PRepMetadata with id = rand(), group = partialGroup = null.
	 * 
	 * @param oid - the seed for the id creation.
	 * @return the newly created metadata.
	 */
	public PartialReplicationOID generateOID(int oid);
}
