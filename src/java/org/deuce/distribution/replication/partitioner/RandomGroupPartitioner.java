package org.deuce.distribution.replication.partitioner;

import java.util.Collection;
import java.util.Set;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;

/**
 * @author jaasilva
 * 
 */
public class RandomGroupPartitioner extends Partitioner implements
		GroupPartitioner
{
	public RandomGroupPartitioner()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.replication.partitioner.GroupPartitioner#init()
	 */
	@Override
	public void init()
	{
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.replication.partitioner.GroupPartitioner#
	 * partitionGroups(java.util.Collection)
	 */
	@Override
	public Set<Group> partitionGroups(Collection<Address> members, int groups)
	{
		// TODO Auto-generated method stub
		return null;
	}
}
