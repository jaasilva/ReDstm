package org.deuce.distribution.replication.partial.oid;

import java.nio.ByteBuffer;

import org.deuce.distribution.TribuDSTM;
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
	private java.util.UUID id; // should be unique in the entire system
	private Group group;

	/**
	 * Parameterless constructor creates a fresh OID and assumes that the
	 * corresponding groups is the group where this node belongs
	 */
	protected PartialReplicationMetadata()
	{
		id = java.util.UUID.randomUUID();
		group = TribuDSTM.getMyGroup();
	}

	/**
	 * @param group
	 */
	protected PartialReplicationMetadata(Group group)
	{
		id = java.util.UUID.randomUUID();
		this.group = group;
	}

	/**
	 * @param seed
	 */
	protected PartialReplicationMetadata(int seed)
	{
		byte[] name = ByteBuffer.allocate(4).putInt(seed).array();
		id = java.util.UUID.nameUUIDFromBytes(name);
		group = TribuDSTM.getMyGroup();
	}

	/**
	 * @param seed
	 * @param group
	 */
	protected PartialReplicationMetadata(int seed, Group group)
	{
		byte[] name = ByteBuffer.allocate(4).putInt(seed).array();
		id = java.util.UUID.nameUUIDFromBytes(name);
		this.group = group;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{ // metadata only need to have the same id to be considered equal
		return obj instanceof PartialReplicationMetadata
				&& id.equals(((PartialReplicationMetadata) obj).id);
	} // OIDs should unique in the entire system

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return String.format("%s: (%s)", id.toString(), group.toString());
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.replication.partial.oid.PartialReplicationOID#
	 * getGroup()
	 */
	@Override
	public Group getGroup()
	{
		return group;
	}
}
