package org.deuce.distribution.replication.partial;

import org.deuce.distribution.replication.OID;
import org.deuce.distribution.replication.group.Group;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

/**
 * Represents a partial replication object identifier.
 * 
 * @author jaasilva
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

	/**
	 * Returns the group of this PRepOID.
	 * 
	 * @return the group os this PRepOID.
	 */
	public Group getGroup();

	public static final String SET_GROUP_METHOD_NAME = "setGroup";
	public static final String SET_GROUP_METHOD_DESC = "(" + Group.DESC + ")"
			+ Type.VOID_TYPE.getDescriptor();

	/**
	 * Sets the group of this PRepOID.
	 * 
	 * @param group - the group to be set.
	 */
	public void setGroup(Group group);

	public static final String GET_PGROUP_METHOD_NAME = "getPartialGroup";
	public static final String GET_PGROUP_METHOD_DESC = "()" + Group.DESC;

	/**
	 * Returns the partial group of this PRepOID. This is important *only* in
	 * OIDs from TxFields (OIDs from normal UniqueObjects have group == pGroup).
	 * 
	 * @return the partial group of this PRepOID.
	 */
	public Group getPartialGroup();

	public static final String SET_PGROUP_METHOD_NAME = "setPartialGroup";
	public static final String SET_PGROUP_METHOD_DESC = "(" + Group.DESC + ")"
			+ Type.VOID_TYPE.getDescriptor();

	/**
	 * Sets the partial group of this PRepOID.
	 * 
	 * @param group - the group to be set.
	 */
	public void setPartialGroup(Group group);

	public String toString();

	public boolean equals(Object obj);

	public int hashCode();

	/**
	 * Determines if this PRepOID has been published to the network.
	 * 
	 * @return true if PRepOID is published, false otherwise.
	 */
	public boolean isPublished();

	/**
	 * Sets this PRepOID to be published.
	 */
	public void publish();
}
