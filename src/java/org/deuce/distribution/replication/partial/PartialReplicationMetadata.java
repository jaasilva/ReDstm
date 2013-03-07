package org.deuce.distribution.replication.partial;

import java.nio.ByteBuffer;

/**
 * @author jaasilva
 * 
 */
public class PartialReplicationMetadata implements PartialReplicationOID
{
	private static final long serialVersionUID = -439466285256483698L;
	private java.util.UUID id;
	// private Group group;

	protected PartialReplicationMetadata()
	{
		id = java.util.UUID.randomUUID();
		// TODO arranjar grupo
	}

	protected PartialReplicationMetadata(int seed)
	{
		byte[] name = ByteBuffer.allocate(4).putInt(seed).array();
		id = java.util.UUID.nameUUIDFromBytes(name);
		// TODO arranjar grupo
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof PartialReplicationMetadata
				&& id.equals(((PartialReplicationMetadata) obj).id);
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public String toString()
	{
		return id.toString(); // TODO adicionar nome do grupo??
	}
}
