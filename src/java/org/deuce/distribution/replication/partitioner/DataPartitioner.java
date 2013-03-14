package org.deuce.distribution.replication.partitioner;

import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.group.Group;

/**
 * @author jaasilva
 * 
 */
public interface DataPartitioner
{
	public void init();

	public Group publishTo(UniqueObject obj);
}
