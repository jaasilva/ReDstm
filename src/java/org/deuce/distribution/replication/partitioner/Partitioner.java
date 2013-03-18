package org.deuce.distribution.replication.partitioner;

import java.util.LinkedList;
import java.util.List;
import org.deuce.distribution.replication.group.Group;

/**
 * @author jaasilva
 * 
 */
public abstract class Partitioner
{
	protected static List<Group> groups;
	protected static Group myGroup;

	/**
	 * 
	 */
	public Partitioner()
	{
		groups = new LinkedList<Group>();
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
