package org.deuce.distribution.replication.partial.oid;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class PartialReplicationMetadataFactory implements
		PartialReplicationOIDFactory
{
	@Override
	public PartialReplicationOID generateOID()
	{ // Requires the insertion of the group and ID later.
		return new PartialReplicationMetadata();
	}

	@Override
	public PartialReplicationOID generateFullReplicationOID()
	{ // Requires the insertion of the ID later.
		Group group = new PartialReplicationGroup(TribuDSTM.getAllMembers());
		return new PartialReplicationMetadata(group);
	}

	@Override
	public PartialReplicationOID generateFullReplicationOID(int oid)
	{
		Group group = new PartialReplicationGroup(TribuDSTM.getAllMembers());
		return new PartialReplicationMetadata(oid, group);
	}

	@Override
	public PartialReplicationOID generateOID(int oid)
	{ // Requires the insertion of the group later.
		return new PartialReplicationMetadata(oid);
	}
}
