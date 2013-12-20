package org.deuce.distribution.replication.partitioner.group;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class RoundRobinGroupPartitioner extends Partitioner
{
	private static final Logger LOGGER = Logger
			.getLogger(RoundRobinGroupPartitioner.class);

	public RoundRobinGroupPartitioner()
	{
		super();
	}

	@Override
	public void partitionGroups(Collection<Address> members)
	{
		List<Group> groupsList = super.getGroups();
		int groups = super.getNumGroups();
		int group = 0;
		for (Address a : members)
		{
			Group selectedGroup = groupsList.get(group % groups);
			selectedGroup.add(a);
			if (a.isLocal())
			{
				super.setLocalGroup(selectedGroup);
			}
			group++;
		}
		LOGGER.warn(super.toString());
	}
}
