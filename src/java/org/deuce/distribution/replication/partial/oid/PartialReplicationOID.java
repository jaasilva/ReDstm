package org.deuce.distribution.replication.partial.oid;

import org.deuce.distribution.replication.OID;
import org.deuce.distribution.replication.group.Group;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public interface PartialReplicationOID extends OID
{
	public final static String NAME = Type
			.getInternalName(PartialReplicationOID.class);
	public final static String DESC = Type
			.getDescriptor(PartialReplicationOID.class);

	public static final String GET_GROUP_METHOD_NAME = "getGroup";
	public static final String GET_GROUP_METHOD_DESC = "()" + Group.DESC;

	public Group getGroup();

	public static final String SET_GROUP_METHOD_NAME = "setGroup";
	public static final String SET_GROUP_METHOD_DESC = "(" + Group.DESC + ")"
			+ Type.VOID_TYPE.getDescriptor();

	public void setGroup(Group group);
	
	public static final String GET_PGROUP_METHOD_NAME = "getPartialGroup";
	public static final String GET_PGROUP_METHOD_DESC = "()" + Group.DESC;

	public Group getPartialGroup();

	public static final String SET_PGROUP_METHOD_NAME = "setPartialGroup";
	public static final String SET_PGROUP_METHOD_DESC = "(" + Group.DESC + ")"
			+ Type.VOID_TYPE.getDescriptor();

	public void setPartialGroup(Group group);

	public String toString();

	public boolean equals(Object obj);

	public int hashCode();

	public boolean isPublished();

	public void publish();
}
