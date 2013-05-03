package org.deuce.distribution.replication.partitioner;

import java.util.ArrayList;
import java.util.List;

import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public abstract class Partitioner
{
	private static List<Group> groups = new ArrayList<Group>();
	private static Group myGroup;

	public Partitioner()
	{
	}

	public List<Group> getGroups()
	{
		return groups;
	}

	public Group getLocalGroup()
	{
		return myGroup;
	}

	public void setLocalGroup(Group group)
	{
		myGroup = group;
	}
}
