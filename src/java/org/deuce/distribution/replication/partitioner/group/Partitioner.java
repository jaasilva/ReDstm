package org.deuce.distribution.replication.partitioner.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public abstract class Partitioner implements GroupPartitioner
{
	private List<Group> groups;
	private Group localGroup;
	private int numGroups;

	public Partitioner()
	{
		this.groups = null;
		this.localGroup = null;
		this.numGroups = 0;
	}

	@Override
	public void init(int numGroups)
	{
		this.groups = new ArrayList<Group>(numGroups);
		this.localGroup = null;
		this.numGroups = numGroups;

		for (int i = 0; i < numGroups; i++)
		{
			this.groups.add(new PartialReplicationGroup(i));
		}
	}

	@Override
	public List<Group> getGroups()
	{
		return this.groups;
	}

	@Override
	public Group getGroup(int id)
	{
		return this.groups.get(id);
	}

	@Override
	public Group getLocalGroup()
	{
		return this.localGroup;
	}

	public void setLocalGroup(Group group)
	{
		this.localGroup = group;
	}

	@Override
	public int getNumGroups()
	{
		return this.numGroups;
	}

	@Override
	public abstract void partitionGroups(Collection<Address> members);

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("----------- GROUPS (");
		sb.append(getNumGroups());
		sb.append(") -----------\n");
		for (Group g : groups)
		{
			if (g.isLocal())
			{
				sb.append("*G");
			}
			else
			{
				sb.append("G");
			}
			sb.append(g.getId());
			sb.append(": ");
			sb.append(g.toStringMembers());
			sb.append("\n");
		}
		sb.append("----------------------------------");
		return sb.toString();
	}
}
