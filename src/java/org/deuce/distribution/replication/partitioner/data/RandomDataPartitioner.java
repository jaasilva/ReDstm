package org.deuce.distribution.replication.partitioner.data;

import java.util.Random;

import org.apache.log4j.Logger;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partitioner.Partitioner;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class RandomDataPartitioner extends Partitioner implements
		DataPartitioner
{
	private static final Logger LOGGER = Logger
			.getLogger(RandomDataPartitioner.class);
	private int groups;
	private Random rand;

	public RandomDataPartitioner()
	{
		super();
		rand = new Random();

		LOGGER.info("RandomDataPartitioner created");
	}

	@Override
	public void init()
	{
		groups = super.getGroups().size();

		LOGGER.info("RandomDataPartitioner initialized");
	}

	@Override
	public Group publishTo(UniqueObject obj)
	{
		int r = rand.nextInt(groups + 1);
		Group res = super.getGroups().get(r);

		LOGGER.info(String.format("Publish obj(%s) to group(%s)", obj, res));

		return res;
	}
}
