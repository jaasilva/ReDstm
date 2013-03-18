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
	protected static List<Group> groups;
	protected static Group myGroup;

	/**
	 * 
	 */
	public Partitioner()
	{
		groups = new ArrayList<Group>();
	}

	/**
	 * @return
	 */
	public List<Group> getGroups()
	{
		return groups;
	}

	/**
	 * @return
	 */
	public Group getMyGroup()
	{
		return myGroup;
	}

	/**
	 * @param group
	 */
	public void setMyGroup(Group group)
	{
		myGroup = group;
	}
}
