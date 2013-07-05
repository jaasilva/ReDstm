package org.deuce.distribution.replication.partitioner.data;

import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public interface DataPartitioner
{
	public void init(); // is called always after the groups have been formed

	public Group publishTo(UniqueObject obj);
}
