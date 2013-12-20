package org.deuce.distribution.replication.partitioner.group;

import java.util.Collection;
import java.util.List;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public interface GroupPartitioner
{
	public void init(int numGroups);

	public void partitionGroups(Collection<Address> members);

	public List<Group> getGroups();

	public Group getGroup(int id);

	public Group getLocalGroup();

	public int getNumGroups();

	public String toString();
}
