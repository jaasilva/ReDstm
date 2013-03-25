package org.deuce.distribution.replication.group;

import java.util.List;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public interface Group
{
	/**
	 * @return
	 */
	public List<Address> getAddresses();

	/**
	 * @param addr
	 * @return
	 */
	public boolean contains(Address addr);

	/**
	 * @param addr
	 * @return
	 */
	public boolean addAddress(Address addr);

	/**
	 * @param addrs
	 * @return
	 */
	public boolean addAddresses(List<Address> addrs);

	/**
	 * @param addr
	 * @return
	 */
	public boolean removeAddress(Address addr);

	/**
	 * @return
	 */
	public int getSize();

	/**
	 * @return
	 */
	public String toString();

	/**
	 * @param obj
	 * @return
	 */
	public boolean equals(Object obj);

	/**
	 * @return
	 */
	public int hashCode();
}
