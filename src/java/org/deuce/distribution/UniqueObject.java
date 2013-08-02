package org.deuce.distribution;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

/**
 * Every distributed object must implement this interface. It represents a
 * distributed object in the system.
 * 
 * @author tvale
 * 
 */
@ExcludeTM
public interface UniqueObject extends Serializable
{
	public static final String DESC = Type.getDescriptor(UniqueObject.class);
	public static final String NAME = Type.getInternalName(UniqueObject.class);

	public static final String METADATA_FIELD_NAME = "#metadata";

	public static final String GETMETADATA_METHOD_NAME = "getMetadata";
	public static final String GETMETADATA_METHOD_DESC = "()"
			+ Type.getDescriptor(ObjectMetadata.class);

	/**
	 * Returns the object's global identifier.
	 * 
	 * @return the global object identifier.
	 */
	public ObjectMetadata getMetadata();

	public static final String SETMETADATA_METHOD_NAME = "setMetadata";
	public static final String SETMETADATA_METHOD_DESC = "("
			+ ObjectMetadata.DESC + ")V";

	/**
	 * Sets the object's global identifier.
	 * 
	 * @param oid The global object identifier.
	 */
	public void setMetadata(ObjectMetadata metadata);

	/**
	 * Returns the object after being deserialized.
	 * 
	 * @return the object after being deserialized.
	 * @throws ObjectStreamException
	 */
	abstract Object writeReplace() throws ObjectStreamException;

	/**
	 * Returns the object to be serialized.
	 * 
	 * @return the object to be serialized.
	 * @throws ObjectStreamException
	 */
	abstract Object readResolve() throws ObjectStreamException;
}
