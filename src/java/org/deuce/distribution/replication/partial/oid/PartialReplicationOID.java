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
{
	public Group getGroup();

	public void setGroup(Group group);

	public String toString();

	public boolean equals(Object obj);

	public int hashCode();

	public boolean isPublished();

	public void publish();
}
