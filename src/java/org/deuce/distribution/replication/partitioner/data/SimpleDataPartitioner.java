package org.deuce.distribution.replication.partitioner.data;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SimpleDataPartitioner implements DataPartitioner
{
	public SimpleDataPartitioner()
	{
		super();
	}

	@Override
	public void init()
	{
	}

	@Override
	public Group publishTo(UniqueObject obj)
	{
		return TribuDSTM.getLocalGroup();
	}
}
