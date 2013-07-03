package org.deuce.distribution.replication.group;

import java.util.Collection;
import java.util.HashSet;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class PartialReplicationGroup implements Group
{
	private static final long serialVersionUID = 1L;
	private Collection<Address> addrs;
	int id;

	public PartialReplicationGroup(int id)
	{
		this.id = id;
		this.addrs = new HashSet<Address>();
	}

	public PartialReplicationGroup(Collection<Address> addrs)
	{
		this.addrs = addrs;
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
	public void set(Collection<Address> addrs)
	{
		this.addrs = addrs;
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
		if (this.addrs.size() == 0)
		{
			return "[]";
		}

		StringBuilder sb = new StringBuilder("[");
		for (Address a : this.addrs)
		{
			sb.append(a);
			sb.append(" ");
		}
		sb.insert(sb.length() - 1, "]");

		return sb.toString().trim();
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
		return this.addrs.hashCode();
	}

	@Override
	public int getId()
	{
		return this.id;
	}

	@Override
	public boolean isLocal()
	{
		return TribuDSTM.isLocalGroup(this); // XXX
	}
}
