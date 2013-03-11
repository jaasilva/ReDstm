package org.deuce.distribution.replication.partial.oid;

import java.nio.ByteBuffer;

import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class PartialReplicationMetadata implements PartialReplicationOID
{
	private static final long serialVersionUID = -439466285256483698L;
	private java.util.UUID id;
	private Group group;

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
		return id.toString() + ": " + group.toString();
	}
}
