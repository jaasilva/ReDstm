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
{ // XXX REVIEW EVERYTHING!!!!
	private static final long serialVersionUID = 1L;
	private java.util.UUID id;
	private Group group;

	public PartialReplicationMetadata()
	{ // Requires the insertion of the group and ID later.
		this.id = null;
		this.group = null;
	}

	public PartialReplicationMetadata(Group group)
	{ // Requires the insertion of the ID later.
		this.id = null;
		this.group = group;
	}

	public PartialReplicationMetadata(int seed)
	{ // Requires the insertion of the group later.
		byte[] name = ByteBuffer.allocate(4).putInt(seed).array();
		this.id = java.util.UUID.nameUUIDFromBytes(name);
		this.group = null;
	}

	public PartialReplicationMetadata(int seed, Group group)
	{
		byte[] name = ByteBuffer.allocate(4).putInt(seed).array();
		this.id = java.util.UUID.nameUUIDFromBytes(name);
		this.group = group;
	}

	@Override
	public Group getGroup()
	{
		return this.group;
	}

	@Override
	public void setGroup(Group group)
	{
		this.group = group;
	}

	@Override
	public void generateId()
	{ // Only if ID is not defined yet
		if (id == null)
		{
			this.id = java.util.UUID.randomUUID();
		}
	}

	@Override
	public boolean isIdAssigned()
	{
		return this.id != null;
	}

	@Override
	public String toString()
	{ // id:[group]
		return String.format("%s:%s", this.id, this.group);
	}

	@Override
	public boolean equals(Object obj)
	{ // PRepMetadata only need to have the same id to be considered equal
		return obj instanceof PartialReplicationMetadata
				&& this.id.equals(((PartialReplicationMetadata) obj).id);
	}

	@Override
	public int hashCode()
	{
		return this.id.hashCode();
	}
}
