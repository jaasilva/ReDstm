package org.deuce.distribution.replication.partial.oid;

import org.deuce.distribution.TribuDSTM;
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
	{ // Requires the insertion of the group later.
		return new PartialReplicationMetadata();
	}

	@Override
	public PartialReplicationOID generateFullReplicationOID()
	{
		return new PartialReplicationMetadata(TribuDSTM.ALL);
	}

	@Override
	public PartialReplicationOID generateFullReplicationOID(int oid)
	{
		return new PartialReplicationMetadata(oid, TribuDSTM.ALL);
	}

	@Override
	public PartialReplicationOID generateOID(int oid)
	{ // Requires the insertion of the group later.
		return new PartialReplicationMetadata(oid);
	}
}
