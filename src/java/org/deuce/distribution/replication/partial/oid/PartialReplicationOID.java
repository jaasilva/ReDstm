package org.deuce.distribution.replication.partial.oid;

import org.deuce.distribution.replication.OID;
import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public interface PartialReplicationOID extends OID
{ // XXX REVIEW EVERYTHING!!!!
	public Group getGroup();

	public void setGroup(Group group);

	public void generateId();

	public boolean isIdAssigned();

	public String toString();

	public boolean equals(Object obj);

	public int hashCode();
}
