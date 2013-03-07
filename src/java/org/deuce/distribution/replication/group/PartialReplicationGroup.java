package org.deuce.distribution.replication.group;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.deuce.distribution.groupcomm.Address;

/**
 * @author jaasilva
 * 
 */
public class PartialReplicationGroup implements Group
{ // TODO PartialReplicationGroup
	private Set<Address> nodes;

	public PartialReplicationGroup()
	{
		this.nodes = new TreeSet<Address>();
	}

	public PartialReplicationGroup(Collection<? extends Address> nodes)
	{
		this.nodes = new TreeSet<Address>(nodes);
	}

	public Set<Address> getNodes()
	{
		return nodes;
	}

	public void setNodes(Set<Address> nodes)
	{
		this.nodes = nodes;
	}

	public boolean removeNode(Address node)
	{
		return nodes.remove(node);
	}

	public boolean contains(Address node)
	{
		return nodes.contains(node);
	}

	public boolean addNode(Address node)
	{
		return nodes.add(node);
	}
}
