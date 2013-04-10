package org.deuce.distribution.replication.partitioner.group;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partitioner.Partitioner;
import org.deuce.hashing.Hashing;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class RoundRobinGroupPartitioner extends Partitioner implements
		GroupPartitioner
{
	private static final Logger LOGGER = Logger
			.getLogger(RoundRobinGroupPartitioner.class);
	private Hashing hash;

	
	public RoundRobinGroupPartitioner()
	{
		super();

		// TODO
	}

	@Override
	public void partitionGroups(Collection<Address> members, int groups)
	{
		//TODO
	}

	@Override
	public List<Group> getGroups()
	{
		return super.getGroups();
	}

	@Override
	public Group getMyGroup()
	{
		return super.getMyGroup();
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder("{");
		for (Group g : getGroups())
		{
			sb.append(g);
			sb.append(" ");
		}
		sb.insert(sb.length() - 1, "}");

		return sb.toString();
	}
}
