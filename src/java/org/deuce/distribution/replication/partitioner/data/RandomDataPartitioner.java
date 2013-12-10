package org.deuce.distribution.replication.partitioner.data;

import java.util.Random;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class RandomDataPartitioner implements DataPartitioner
{
	private int groups;
	private Random rand;

	public RandomDataPartitioner()
	{
		super();
		rand = new Random();
	}

	@Override
	public void init()
	{
		groups = TribuDSTM.getNumGroups();
	}

	@Override
	public Group publishTo(UniqueObject obj)
	{
		int r = rand.nextInt(groups);
		return TribuDSTM.getGroup(r);
	}
}
