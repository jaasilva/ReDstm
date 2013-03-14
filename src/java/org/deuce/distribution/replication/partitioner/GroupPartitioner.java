package org.deuce.distribution.replication.partitioner;

import java.util.Collection;
import java.util.Set;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;

/**
 * @author jaasilva
 * 
 */
public interface GroupPartitioner
{
	public void init();
// TODO isto nao deve ser void?
	public Set<Group> partitionGroups(Collection<Address> members, int groups);
}
