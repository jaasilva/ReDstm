package org.deuce.distribution.replication.partitioner.group;

import java.util.Collection;
import java.util.List;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partitioner.Partitioner;
import org.deuce.hashing.Hashing;

/**
 * @author jaasilva
 * 
 */
public class RandomGroupPartitioner extends Partitioner implements
		GroupPartitioner
{
	private Hashing hash;

	/**
	 * 
	 */
	public RandomGroupPartitioner()
	{
		super();

		String className = System.getProperty(
				"tribu.distributed.GroupPartitionerHashClass",
				"org.deuce.hashing.MD5GuavaHashFunction");

		try
		{
			@SuppressWarnings("unchecked")
			Class<? extends Hashing> hashClass = (Class<? extends Hashing>) Class
					.forName(className);
			hash = hashClass.newInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.replication.partitioner.GroupPartitioner#
	 * partitionGroups(java.util.Collection)
	 */
	@Override
	public void partitionGroups(Collection<Address> members, int groups)
	{ // XXX Assumes the correct match between the number of nodes and groups
		List<Group> g = getGroups();
		try
		{
			Class<? extends Group> gClass = TribuDSTM.getGroupClass();
			for (int i = 0; i < groups; i++)
			{
				g.add(gClass.newInstance());
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		// TODO guardar o mygroup
		for (Address a : members)
		{
			int selected = hash.consistentHash(a.toString(), groups);
			g.get(selected).addAddress(a);
			System.out.println(a + " >> " + selected);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.replication.partitioner.Partitioner#getGroups()
	 */
	@Override
	public List<Group> getGroups()
	{
		return super.getGroups();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.replication.partitioner.Partitioner#getMyGroup()
	 */
	@Override
	public Group getMyGroup()
	{
		return super.getMyGroup();
	}
}
