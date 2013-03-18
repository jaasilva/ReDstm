package org.deuce.distribution.replication.partitioner.data;

import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partitioner.Partitioner;

/**
 * @author jaasilva
 * 
 */
public class SimpleDataPartitioner extends Partitioner implements
		DataPartitioner
{
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
		return getMyGroup();
	}
}
