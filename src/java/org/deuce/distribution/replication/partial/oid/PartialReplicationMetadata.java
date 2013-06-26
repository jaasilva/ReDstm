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
	private static final long serialVersionUID = 1L;
	private java.util.UUID id;
	private Group group;
	private Group partialGroup;
	private boolean isPublished;

	public PartialReplicationMetadata()
	{ // Requires the insertion of the group later.
		this.id = java.util.UUID.randomUUID();
		this.group = null;
		this.partialGroup = null;
		this.isPublished = false;
	}

	public PartialReplicationMetadata(Group group)
	{
		this.id = java.util.UUID.randomUUID();
		this.group = group;
		this.partialGroup = group;
		this.isPublished = false;
	}

	public PartialReplicationMetadata(int seed)
	{ // Requires the insertion of the group later.
		byte[] name = ByteBuffer.allocate(4).putInt(seed).array();
		this.id = java.util.UUID.nameUUIDFromBytes(name);
		this.group = null;
		this.partialGroup = null;
		this.isPublished = false;
	}

	public PartialReplicationMetadata(int seed, Group group)
	{
		byte[] name = ByteBuffer.allocate(4).putInt(seed).array();
		this.id = java.util.UUID.nameUUIDFromBytes(name);
		this.group = group;
		this.partialGroup = group;
		this.isPublished = false;
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
	public Group getPartialGroup()
	{
		return this.partialGroup;
	}

	@Override
	public void setPartialGroup(Group group)
	{
		this.partialGroup = group;
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

	@Override
	public boolean isPublished()
	{
		return this.isPublished;
	}

	@Override
	public void publish()
	{
		this.isPublished = true;
	}
}
