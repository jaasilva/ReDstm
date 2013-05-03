package org.deuce.distribution.replication.group;

import java.util.Collection;
import java.util.HashSet;
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
	private Set<Address> addrs;

	public PartialReplicationGroup()
	{
		this.addrs = new HashSet<Address>();
	}

	public PartialReplicationGroup(Collection<? extends Address> addrs)
	{
		this.addrs = new HashSet<Address>(addrs);
	}

	@Override
	public Collection<Address> getAll()
	{
		return this.addrs;
	}

	@Override
	public boolean contains(Address addr)
	{
		return this.addrs.contains(addr);
	}

	@Override
	public boolean add(Address addr)
	{
		return this.addrs.add(addr);
	}

	@Override
	public boolean addAll(Collection<Address> addrs)
	{
		return this.addrs.addAll(addrs);
	}

	@Override
	public boolean remove(Address addr)
	{
		return this.addrs.remove(addr);
	}

	@Override
	public int size()
	{
		return this.addrs.size();
	}

	@Override
	public Group union(Group other)
	{
		HashSet<Address> union = new HashSet<Address>(this.addrs);
		union.addAll(other.getAll());
		return new PartialReplicationGroup(union);
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder("GROUP:[");
		for (Address a : addrs)
		{
			sb.append(a);
			sb.append(" ");
		}
		sb.insert(sb.length() - 1, "]");

		return sb.toString();
	}

	public boolean equals(Object obj)
	{
		return (obj instanceof PartialReplicationGroup)
				&& (((PartialReplicationGroup) obj).size() == this.size())
				&& (((PartialReplicationGroup) obj).addrs
						.containsAll(this.addrs));
	}

	public int hashCode()
	{
		return addrs.hashCode();
	}
}
