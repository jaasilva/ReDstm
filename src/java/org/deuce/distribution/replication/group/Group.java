package org.deuce.distribution.replication.group;

import java.io.Serializable;
import java.util.Collection;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

/**
 * This interface represents a group of addresses.
 * 
 * @author jaasilva
 */
@ExcludeTM
public interface Group extends Serializable
{
	public final static String NAME = Type.getInternalName(Group.class);
	public final static String DESC = Type.getDescriptor(Group.class);

	public final static short ALL = -1;
	public final static short NIL = -2;

	/**
	 * Returns a collection with the addresses of all the members of the group.
	 * 
	 * @return the group members.
	 */
	public Collection<Address> getAll();

	/**
	 * Determines if an address is in this group.
	 * 
	 * @param addr - the address to check.
	 * @return true if the address is in this group, false otherwise.
	 */
	public boolean contains(Address addr);

	/**
	 * Adds an address to this group.
	 * 
	 * @param addr - the address to add.
	 * @return true if is was possible to add the address, false otherwise.
	 */
	public boolean add(Address addr);

	/**
	 * Adds an entire collection to this group.
	 * 
	 * @param addrs - the collection to add.
	 * @return true if is was possible to add the addresses, false otherwise.
	 */
	public boolean addAll(Collection<Address> addrs);

	/**
	 * Removes an address from this group.
	 * 
	 * @param addr - the address to remove.
	 * @return true if is was possible to remove the address, false otherwise.
	 */
	public boolean remove(Address addr);

	/**
	 * Sets the reference to the addresses set.
	 * 
	 * @param addrs - the addresses set.
	 */
	public void set(Collection<Address> addrs);

	/**
	 * Returns the number of addresses in this group.
	 * 
	 * @return the number of addresses.
	 */
	public int size();

	/**
	 * Returns a readable string representing the group.
	 * 
	 * @return a human-readable string.
	 */
	public String toString();

	/**
	 * Determines if an object is equals to this group.
	 * 
	 * @param obj - the object to check.
	 * @return true if the object is equals to this group, false otherwise.
	 */
	public boolean equals(Object obj);

	/**
	 * Returns the group hash code.
	 * 
	 * @return this group's hash code.
	 */
	public int hashCode();

	/**
	 * Returns a new group made of the union of this group with the group given
	 * has a parameter.
	 * 
	 * @param other - the group to do the union with.
	 * @return a new group with the union.
	 */
	public Group union(Group other);

	/**
	 * Returns the ID of this group.
	 * 
	 * @return the ID of this group.
	 */
	public int getId();

	/**
	 * Determines if a group is local.
	 * 
	 * @return true if the group is local, false otherwise.
	 */
	public boolean isLocal();

	/**
	 * Determines if a group is ALL.
	 * 
	 * @return true if the group is ALL, false otherwise.
	 */
	public boolean isAll();
}
