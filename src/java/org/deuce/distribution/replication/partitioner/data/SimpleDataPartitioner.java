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

	/**
	 * 
	 */
	public SimpleDataPartitioner()
	{
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.replication.partitioner.DataPartitioner#init()
	 */
	@Override
	public void init()
	{
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.replication.partitioner.DataPartitioner#publishTo
	 * (org.deuce.distribution.UniqueObject)
	 */
	@Override
	public Group publishTo(UniqueObject obj)
	{
		Group res = super.getMyGroup();
		LOGGER.debug(String.format("Publish obj:%s to group:%s", obj, res));
		return res;
	}
}
