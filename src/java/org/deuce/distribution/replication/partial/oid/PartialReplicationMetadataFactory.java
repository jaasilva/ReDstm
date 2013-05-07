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
{ // XXX REVIEW EVERYTHING!!!!
	@Override
	public PartialReplicationOID generateOID()
	{ // Requires the insertion of the group and ID later.
		return new PartialReplicationMetadata();
	}

	@Override
	public PartialReplicationOID generateFullReplicationOID()
	{ // Requires the insertion of the ID later.
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
