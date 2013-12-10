package org.deuce.distribution.replication.partial.oid;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class PartialReplicationMetadataFactory implements
		PartialReplicationOIDFactory
{
	@Override
	public PartialReplicationOID generateOID()
	{ // id = rand(), group = ALL, partialGroup = []
		final Group group = new PartialReplicationGroup(TribuDSTM.ALL);
		final PartialReplicationGroup partialGroup = new PartialReplicationGroup();
		final PartialReplicationMetadata metadata = new PartialReplicationMetadata(
				group);
		metadata.setPartialGroup(partialGroup);
		return metadata;
	}

	@Override
	public PartialReplicationOID generateFullReplicationOID()
	{ // id = rand(), group = partialGroup = ALL
		return new PartialReplicationMetadata(new PartialReplicationGroup(
				TribuDSTM.ALL));
	}

	@Override
	public PartialReplicationOID generateFullReplicationOID(int oid)
	{ // id = rand(oid), group = partialGroup = ALL
		return new PartialReplicationMetadata(oid, new PartialReplicationGroup(
				TribuDSTM.ALL));
	}

	@Override
	public PartialReplicationOID generateOID(int oid)
	{ // id = rand(), group = partialGroup = null
		return new PartialReplicationMetadata(oid);
	}
}
