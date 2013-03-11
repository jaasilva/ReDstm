package org.deuce.distribution.replication;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

/**
 * API of a distributed object identifier.
 * 
 * @author Tiago Vale
 */
@ExcludeTM
public interface OID extends ObjectMetadata
{
	public static final String DESC = Type.getDescriptor(OID.class);
	public static final String NAME = Type.getInternalName(OID.class);

	public boolean equals(Object obj);

	public int hashCode();

	/**
	 * Textual, human-readable representation of the identifier.
	 * 
	 * @return Human-readable representation of the identifier.
	 */
	public String toString();
}
