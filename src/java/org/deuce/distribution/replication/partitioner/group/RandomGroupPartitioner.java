package org.deuce.distribution.replication.partitioner.group;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;
import org.deuce.distribution.replication.partitioner.Partitioner;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class RandomGroupPartitioner extends Partitioner implements
		GroupPartitioner
{
	private static final Logger LOGGER = Logger
			.getLogger(RandomGroupPartitioner.class);
	private Random rand;

	public RandomGroupPartitioner()
	{
		super();
		rand = new Random();
	}

	@Override
	public void partitionGroups(Collection<Address> members, int groups)
	{
		List<Group> g = super.getGroups();
		for (int i = 0; i < groups; i++)
		{
			g.add(new PartialReplicationGroup(i));
		}

		for (Address a : members)
		{
			int selected = rand.nextInt() % groups;
			Group selectedGroup = g.get(selected);
			selectedGroup.add(a);

			if (a.isLocal())
			{
				super.setLocalGroup(selectedGroup);
			}
		}

		LOGGER.warn("> GROUPS:" + toString());
	}
}
