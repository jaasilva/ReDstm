package org.deuce.distribution.replication.partial.oid;

import org.deuce.distribution.TribuDSTM;
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

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.replication.partial.oid.PartialReplicationOIDFactory
	 * #generateFullReplicationOID()
	 */
	@Override
	public PartialReplicationOID generateFullReplicationOID()
	{
		try
		{
			Group group = TribuDSTM.getGroupClass().newInstance();
			group.addAddresses(TribuDSTM.getAllMembers());
			return new PartialReplicationMetadata(group);
		}
		catch (Exception e)
		{
			System.err.println("Couldn't create group class.");
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.replication.partial.oid.PartialReplicationOIDFactory
	 * #generateFullReplicationOID(int)
	 */
	@Override
	public PartialReplicationOID generateFullReplicationOID(int oid)
	{
		try
		{
			Group group = TribuDSTM.getGroupClass().newInstance();
			group.addAddresses(TribuDSTM.getAllMembers());
			return new PartialReplicationMetadata(oid, group);
		}
		catch (Exception e)
		{
			System.err.println("Couldn't create group class.");
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}
}
