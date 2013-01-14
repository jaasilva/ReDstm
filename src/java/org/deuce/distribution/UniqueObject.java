package org.deuce.distribution;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public interface UniqueObject extends Serializable {
//public abstract class UniqueObject implements Serializable {
//	private static final long serialVersionUID = 5474769159884684568L;
//
	public static final String DESC = Type.getDescriptor(UniqueObject.class);
	public static final String NAME = Type.getInternalName(UniqueObject.class);

	public static final String METADATA_FIELD_NAME = "#metadata";

//	private ObjectMetadata metadata;
//
//	public UniqueObject() {
//	}
//
//	public UniqueObject(IContext ctx) {
//	}
//
	public static final String GETMETADATA_METHOD_NAME = "getMetadata";
	public static final String GETMETADATA_METHOD_DESC = "()"
			+ Type.getDescriptor(ObjectMetadata.class);
	/**
	 * Returns the object's global identifier.
	 * 
	 * @return the global object identifier.
	 */
	public ObjectMetadata getMetadata();
//	{
//		return metadata;
//	}
//
	public static final String SETMETADATA_METHOD_NAME = "setMetadata";
	public static final String SETMETADATA_METHOD_DESC = "("
			+ ObjectMetadata.DESC + ")V";
	/**
	 * Sets the object's global identifier.
	 * 
	 * @param oid
	 *            The global object identifier.
	 */
	public void setMetadata(ObjectMetadata metadata);
//	{
//		this.metadata = metadata;
//	}
//
	abstract Object writeReplace() throws ObjectStreamException;
//	{
//		return TribuDSTM.getObjectSerializer().writeReplaceHook(this);
//	}
//
	abstract Object readResolve() throws ObjectStreamException;
//	{
//		return TribuDSTM.getObjectSerializer().readResolveHook(this);
//	}
}
