package org.deuce.distribution.replication.partitioner.group;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partitioner.Partitioner;
import org.deuce.hashing.Hashing;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class RandomGroupPartitioner extends Partitioner implements
		GroupPartitioner
{
	private static final Logger LOGGER = Logger
			.getLogger(RandomGroupPartitioner.class);
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
		// TODO tenho de verificar se os grupos tem o mesmo tamanho?
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
			System.err.println("Couldn't create group classes.");
			e.printStackTrace();
			System.exit(-1);
		}

		for (Address a : members)
		{
			int selected = hash.consistentHash(a.toString(), groups);
			Group selectedGroup = g.get(selected);
			selectedGroup.addAddress(a);

			if (a.isLocal())
			{
				super.setMyGroup(selectedGroup);
			}
		}
		LOGGER.debug(String.format("NEW GROUPS: %s", toString()));
		System.out.println(toString());
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

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder("{");
		for (Group g : getGroups())
		{
			sb.append(g);
			sb.append(" ");
		}
		sb.insert(sb.length() - 1, "}");

		return sb.toString();
	}
}
