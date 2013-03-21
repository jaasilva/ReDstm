package org.deuce.distribution.replication.partial.oid;

import org.deuce.distribution.replication.OIDFactory;
import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public interface PartialReplicationOIDFactory extends OIDFactory
{
	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.replication.OIDFactory#generateOID()
	 */
	public PartialReplicationOID generateOID();

	/**
	 * @return
	 */
	public PartialReplicationOID generateFullReplicationOID();

	/**
	 * @param oid
	 * @return
	 */
	public PartialReplicationOID generateFullReplicationOID(int oid);

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.replication.OIDFactory#generateOID(int)
	 */
	public PartialReplicationOID generateOID(int oid);

	/**
	 * @param group
	 * @return
	 */
	public PartialReplicationOID generateOID(Group group);

	/**
	 * @param oid
	 * @param group
	 * @return
	 */
	public PartialReplicationOID generateOID(int oid, Group group);
}
