package org.deuce.distribution.replication.partitioner.data;

import java.util.Random;

import org.apache.log4j.Logger;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class RandomDataPartitioner implements DataPartitioner
{
	private static final Logger LOGGER = Logger
			.getLogger(RandomDataPartitioner.class);
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
		int r = rand.nextInt(groups + 1);
		Group res = TribuDSTM.getGroup(r);

		LOGGER.trace(String.format("~ Publish obj(%s) to group(%s) %s",
				obj.getMetadata(), res, obj.getClass().getSimpleName()));

		return res;
	}
}
