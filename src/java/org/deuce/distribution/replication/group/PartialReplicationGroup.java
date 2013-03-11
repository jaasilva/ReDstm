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
{ // TODO verificar se é necessario groupID
	private Set<Address> addresses;

	public PartialReplicationGroup()
	{ // TODO verificar este tamanho
		this.addresses = new HashSet<Address>(50);
	}

	public PartialReplicationGroup(Collection<? extends Address> addresses)
	{
		this.addresses = new HashSet<Address>(addresses);
	}

	public boolean removeAddress(Address addr)
	{
		return addresses.remove(addr);
	}

	public boolean contains(Address addr)
	{
		return addresses.contains(addr);
	}

	public boolean addAddress(Address addr)
	{
		return addresses.add(addr);
	}

	public Collection<Address> getAddresses()
	{
		return addresses;
	}

	public String toString()
	{ // TODO PartialReplicationGroup toString
		return "GROUP";
	}
}
