package org.deuce.distribution.replication.partial.oid;

import java.nio.ByteBuffer;

import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * This class implements the PartialReplicationOID interface. It represents a
 * distribution metadata for partial replication. *Every* metadata object is
 * created with isPublished set to false. Every metadata start being not
 * published.
 * 
 * @author jaasilva
 */
@ExcludeTM
public class PartialReplicationMetadata implements PartialReplicationOID
{
	private static final long serialVersionUID = 1L;
	private java.util.UUID id;
	private Group group;
	private Group partialGroup;
	private boolean isPublished;

	/**
	 * Creates a new PRepMetadata with both group and pGroup = null.
	 */
	public PartialReplicationMetadata()
	{
		this.id = java.util.UUID.randomUUID();
		this.group = null;
		this.partialGroup = null;
		this.isPublished = false;
	}

	/**
	 * Creates a new PRepMetadata with both group and pGroup defined by
	 * parameter.
	 * 
	 * @param group - the defined group.
	 */
	public PartialReplicationMetadata(Group group)
	{
		this.id = java.util.UUID.randomUUID();
		this.group = group;
		this.partialGroup = group;
		this.isPublished = false;
	}

	/**
	 * Creates a new PRepMetadata with both group and pGroup = null. This
	 * metadata id is created deterministically (with seed).
	 * 
	 * @param seed - the seed for the id creation.
	 */
	public PartialReplicationMetadata(int seed)
	{
		byte[] name = ByteBuffer.allocate(4).putInt(seed).array();
		this.id = java.util.UUID.nameUUIDFromBytes(name);
		this.group = null;
		this.partialGroup = null;
		this.isPublished = false;
	}

	/**
	 * Creates a new PRepMetadata with both group and pGroup defined by
	 * parameter. This metadata id is created deterministically (with seed).
	 * 
	 * @param seed - the seed for the id creation.
	 * @param group - the defined group.
	 */
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
	{ // id:[group]:[pgroup]
		return String
				.format("%s:%s:%s", this.id, this.group, this.partialGroup);
	}

	@Override
	public boolean equals(Object obj)
	{
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
