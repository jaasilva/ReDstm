package org.deuce.distribution.replication.partitioner.group;

import java.util.Collection;
import java.util.List;
import java.util.Random;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class RandomGroupPartitioner extends Partitioner
{
	private Random rand;

	public RandomGroupPartitioner()
	{
		super();
		rand = new Random();
	}

	@Override
	public void partitionGroups(Collection<Address> members)
	{
		List<Group> groupsList = super.getGroups();
		int groups = super.getNumGroups();
		for (Address a : members)
		{
			Group selectedGroup = groupsList.get(rand.nextInt() % groups);
			selectedGroup.add(a);
			if (a.isLocal())
			{
				super.setLocalGroup(selectedGroup);
			}
		}
	}
}
