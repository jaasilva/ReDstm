package org.deuce.distribution.replication.partitioner;

import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.group.Group;

/**
 * @author jaasilva
 * 
 */
/**
 * @author jaasilva
 * 
 */
public class SimpleDataPartitioner extends Partitioner implements
		DataPartitioner
{
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
		// TODO Auto-generated method stub

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
		return myGroup;
	}
}
