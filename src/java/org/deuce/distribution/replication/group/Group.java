package org.deuce.distribution.replication.group;

import java.util.Collection;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public interface Group
{
	public Collection<Address> getAll();

	public boolean contains(Address addr);

	public boolean add(Address addr);

	public boolean addAll(Collection<Address> addrs);

	public boolean remove(Address addr);

	public int size();

	public String toString();

	public boolean equals(Object obj);

	public int hashCode();

	public Group union(Group other);
}
