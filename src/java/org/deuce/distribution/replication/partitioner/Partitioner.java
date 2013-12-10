package org.deuce.distribution.replication.partitioner;

import java.util.ArrayList;
import java.util.List;

import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public abstract class Partitioner
{
	private List<Group> groups = new ArrayList<Group>();
	private Group localGroup;

	public Partitioner()
	{
	}

	public List<Group> getGroups()
	{
		return groups;
	}

	public Group getLocalGroup()
	{
		return localGroup;
	}

	public void setLocalGroup(Group group)
	{
		localGroup = group;
	}

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
