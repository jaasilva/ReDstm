package org.deuce.distribution.replication.group;

import java.util.Collection;
import java.util.HashSet;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class PartialReplicationGroup implements Group
{
	private static final long serialVersionUID = 1L;
	private Collection<Address> addrs;
	private int id;

	public PartialReplicationGroup()
	{
		this.id = Group.NIL;
		this.addrs = new HashSet<Address>();
	}

	public PartialReplicationGroup(int id)
	{
		this.id = id;
		this.addrs = new HashSet<Address>();
	}

	public PartialReplicationGroup(Collection<Address> addrs)
	{
		this.id = Group.NIL;
		this.addrs = addrs;
	}

	@Override
	public Collection<Address> getMembers()
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
		union.addAll(other.getMembers());
		return new PartialReplicationGroup(union);
	}

	@Override
	public String toString()
	{
		return "" + id;
	}

	@Override
	public boolean equals(Object obj)
	{
		return (obj instanceof PartialReplicationGroup)
				&& (((PartialReplicationGroup) obj).size() == this.size())
				&& (((PartialReplicationGroup) obj).addrs
						.containsAll(this.addrs));
	}

	@Override
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
		return TribuDSTM.isLocalGroup(this);
	}

	@Override
	public boolean isAll()
	{
		return TribuDSTM.groupIsAll(this);
	}

	@Override
	public String toStringMembers()
	{
		if (size() == 0)
		{
			return "[]";
		}
		StringBuilder sb = new StringBuilder("[");
		for (Address a : this.addrs)
		{
			sb.append(a);
			sb.append(" ");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("]");
		return sb.toString().trim();
	}

	@Override
	public Address getGroupMaster()
	{
		return addrs.iterator().next();
	}
}
