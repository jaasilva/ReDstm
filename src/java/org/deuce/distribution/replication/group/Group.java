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
	public Collection<Address> getAddresses();

	public boolean contains(Address addr);

	public boolean addAddress(Address addr);

	public boolean removeAddress(Address addr);

	public String toString();
}
