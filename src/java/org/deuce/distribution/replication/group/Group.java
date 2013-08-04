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
 * 
 */
@ExcludeTM
public interface Group extends Serializable
{
	public final static String NAME = Type.getInternalName(Group.class);
	public final static String DESC = Type.getDescriptor(Group.class);

	/**
	 * 
	 * 
	 * @return
	 */
	public Collection<Address> getAll();

	/**
	 * @param addr
	 * @return
	 */
	public boolean contains(Address addr);

	/**
	 * @param addr
	 * @return
	 */
	public boolean add(Address addr);

	/**
	 * @param addrs
	 * @return
	 */
	public boolean addAll(Collection<Address> addrs);

	/**
	 * @param addr
	 * @return
	 */
	public boolean remove(Address addr);

	/**
	 * @param addrs
	 */
	public void set(Collection<Address> addrs);

	/**
	 * @return
	 */
	public int size();

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

	/**
	 * @param other
	 * @return
	 */
	public Group union(Group other);

	/**
	 * @return
	 */
	public int getId();

	/**
	 * @return
	 */
	public boolean isLocal();

	/**
	 * @return
	 */
	public boolean isAll();
}
