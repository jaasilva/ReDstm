package org.deuce.distribution.replication.partitioner;

import java.util.LinkedList;
import java.util.List;
import org.deuce.distribution.replication.group.Group;

/**
 * @author jaasilva
 * 
 */
public abstract class Partitioner
{ // TODO verificar melhor estrutura
	protected static List<Group> groups = new LinkedList<Group>();
	protected static Group myGroup = null;

	public Partitioner()
	{
	}

	public List<Group> getGroups()
	{
		return groups;
	}

	public Group getMyGroup()
	{
		return myGroup;
	}
}
