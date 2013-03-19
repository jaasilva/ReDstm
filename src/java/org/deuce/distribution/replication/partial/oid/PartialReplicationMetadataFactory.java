package org.deuce.distribution.replication.partial.oid;

import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class PartialReplicationMetadataFactory implements
		PartialReplicationOIDFactory
{
	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.replication.OIDFactory#generateOID(int)
	 */
	@Override
	public PartialReplicationOID generateOID(int oid)
	{
		return new PartialReplicationMetadata(oid);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.replication.partial.oid.PartialReplicationOIDFactory
	 * #generateOID()
	 */
	@Override
	public PartialReplicationOID generateOID()
	{
		return new PartialReplicationMetadata();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.replication.partial.oid.PartialReplicationOIDFactory
	 * #generateOID(org.deuce.distribution.replication.group.Group)
	 */
	@Override
	public PartialReplicationOID generateOID(Group group)
	{
		return new PartialReplicationMetadata(group);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.replication.partial.oid.PartialReplicationOIDFactory
	 * #generateOID(int, org.deuce.distribution.replication.group.Group)
	 */
	@Override
	public PartialReplicationOID generateOID(int oid, Group group)
	{
		return new PartialReplicationMetadata(oid, group);
	}

}
