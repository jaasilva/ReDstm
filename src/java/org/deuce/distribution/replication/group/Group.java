package org.deuce.distribution.replication.group;

import java.io.Serializable;
import java.util.Collection;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public interface Group extends Serializable {
	public final static String NAME = Type.getInternalName(Group.class);
	public final static String DESC = Type.getDescriptor(Group.class);

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
