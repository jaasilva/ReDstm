package org.deuce.distribution.replication.partitioner.data;

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
public class SimpleDataPartitioner extends Partitioner implements
		DataPartitioner
{
	private static final Logger LOGGER = Logger
			.getLogger(SimpleDataPartitioner.class);

	public SimpleDataPartitioner()
	{
		super();

		LOGGER.info("SimpleDataPartitioner created");
	}

	@Override
	public void init()
	{
		LOGGER.info("SimpleDataPartitioner initialized");
	}

	@Override
	public Group publishTo(UniqueObject obj)
	{
		Group res = super.getLocalGroup();

		LOGGER.info(String.format("Publish obj(%s) to group(%s)", obj, res));

		return res;
	}
}