package org.deuce.distribution.replication.partitioner.group;

import java.util.Collection;
import java.util.List;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public interface GroupPartitioner
{
	/**
	 * @param members
	 * @param groups
	 */
	public void partitionGroups(Collection<Address> members, int groups);

	/**
	 * @return
	 */
	public List<Group> getGroups();

	/**
	 * @return
	 */
	public Group getMyGroup();
}
