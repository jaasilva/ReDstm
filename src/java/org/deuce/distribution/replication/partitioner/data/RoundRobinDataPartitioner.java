package org.deuce.distribution.replication.partitioner.data;

import java.util.concurrent.atomic.AtomicInteger;

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
public class RoundRobinDataPartitioner implements DataPartitioner
{
	private static final Logger LOGGER = Logger
			.getLogger(RoundRobinDataPartitioner.class);
	private AtomicInteger round;
	private int groups;

	public RoundRobinDataPartitioner()
	{
		super();
		round = new AtomicInteger(0);
	}

	@Override
	public void init()
	{
		groups = TribuDSTM.getNumGroups();
	}

	@Override
	public Group publishTo(UniqueObject obj)
	{
		Group res = TribuDSTM.getGroup(round.get() % groups);
		round.getAndIncrement();

		LOGGER.trace(String.format("~ Publish obj(%s) to group(%s) %s",
				obj.getMetadata(), res, obj.getClass().getSimpleName()));

		return res;
	}
}
