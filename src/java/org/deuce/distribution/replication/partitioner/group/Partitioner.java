package org.deuce.distribution.replication.partitioner.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public abstract class Partitioner implements GroupPartitioner
{
	private List<Group> groups;
	private Group localGroup;

	public Partitioner()
	{
		groups = new ArrayList<Group>();
		localGroup = null;
	}

	@Override
	public List<Group> getGroups()
	{
		return groups;
	}

	@Override
	public Group getLocalGroup()
	{
		return localGroup;
	}

	public void setLocalGroup(Group group)
	{
		localGroup = group;
	}

	public abstract void partitionGroups(Collection<Address> members, int groups);

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("{");
		for (Group g : groups)
		{
			sb.append(g + " ");
		}
		sb.insert(sb.length() - 1, "}");

		return sb.toString().trim();
	}
}
