package org.deuce.distribution.replication.group;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class PartialReplicationGroup implements Group
{
	private Set<Address> addresses;
	private java.util.UUID id; // should be unique in the entire system

	/**
	 * 
	 */
	public PartialReplicationGroup()
	{ // CHECKME ver melhor maneira
		this.addresses = new HashSet<Address>(25);
		id = java.util.UUID.randomUUID();
	}

	/**
	 * @param addresses
	 */
	public PartialReplicationGroup(Collection<? extends Address> addresses)
	{
		this.addresses = new HashSet<Address>(addresses);
		id = java.util.UUID.randomUUID();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.replication.group.Group#removeAddress(org.deuce
	 * .distribution.groupcomm.Address)
	 */
	public boolean removeAddress(Address addr)
	{
		return addresses.remove(addr);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.replication.group.Group#contains(org.deuce.
	 * distribution.groupcomm.Address)
	 */
	public boolean contains(Address addr)
	{
		return addresses.contains(addr);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.replication.group.Group#addAddress(org.deuce.
	 * distribution.groupcomm.Address)
	 */
	public boolean addAddress(Address addr)
	{
		return addresses.add(addr);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.replication.group.Group#getAddresses()
	 */
	public List<Address> getAddresses()
	{
		return new ArrayList<Address>(addresses);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuilder sb = new StringBuilder(id.toString());
		sb.append(": [");
		for (Address a : addresses)
		{
			sb.append(a);
			sb.append(" ");
		}
		sb.insert(sb.length() - 1, "]");

		return sb.toString();
	}

	@Override
	public boolean addAddresses(List<Address> addrs)
	{
		return addresses.addAll(addrs);
	}

	@Override
	public boolean equals(Object obj)
	{ // PRepGroup only need to have the same id to be considered equal
		return obj instanceof PartialReplicationGroup
				&& id.equals(((PartialReplicationGroup) obj).id);
	} // ids should be unique in the entire system

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}
}
