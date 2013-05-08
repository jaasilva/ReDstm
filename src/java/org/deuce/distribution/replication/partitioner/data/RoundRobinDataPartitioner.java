package org.deuce.distribution.replication.partitioner.data;

import java.util.concurrent.atomic.AtomicInteger;

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
public class RoundRobinDataPartitioner extends Partitioner implements
		DataPartitioner
{
	private static final Logger LOGGER = Logger
			.getLogger(RoundRobinDataPartitioner.class);
	private AtomicInteger round;
	private int groups;

	public RoundRobinDataPartitioner()
	{
		super();
		round = new AtomicInteger(0);

		LOGGER.info("RoundRobinDataPartitioner created");
	}

	@Override
	public void init()
	{
		groups = super.getGroups().size();

		LOGGER.info("RoundRobinDataPartitioner initialized");
	}

	@Override
	public Group publishTo(UniqueObject obj)
	{
		Group res = super.getGroups().get(round.get() % groups);
		round.getAndIncrement();

		LOGGER.info(String.format("Publish obj(%s) to group(%s)", obj, res));

		return res;
	}
}