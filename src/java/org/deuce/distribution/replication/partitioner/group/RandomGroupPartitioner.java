package org.deuce.distribution.replication.partitioner.group;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;
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

		LOGGER.warn("> Group Partitioner hash class: " + className);
	}

	@Override
	public void partitionGroups(Collection<Address> members, int groups)
	{
		List<Group> g = super.getGroups();
		for (int i = 0; i < groups; i++)
		{
			g.add(new PartialReplicationGroup(i)); // XXX
		}

		for (Address a : members)
		{
			int selected = hash.consistentHash(a.toString(), groups);
			Group selectedGroup = g.get(selected);
			selectedGroup.add(a);

			if (a.isLocal())
			{
				super.setLocalGroup(selectedGroup);
			}
		}

		LOGGER.warn("> GROUPS:" + toString());
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("{");
		for (Group g : getGroups())
		{
			sb.append(g + " ");
		}
		sb.insert(sb.length() - 1, "}");

		return sb.toString().trim();
	}
}
