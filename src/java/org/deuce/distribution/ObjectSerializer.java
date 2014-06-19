package org.deuce.distribution;

import java.io.ObjectStreamException;

import org.deuce.distribution.serialization.GCPayloadException;
import org.deuce.distribution.serialization.JavaSerializer;
import org.deuce.distribution.serialization.Serializer;
import org.deuce.objectweb.asm.Type;
import org.deuce.profiling.Profiler;
import org.deuce.transform.ExcludeTM;

/**
 * This class represents the object serializer used to serialized and
 * de-serialize objects to and from the network.
 * 
 * @author tvale
 */
@ExcludeTM
public abstract class ObjectSerializer
{
	final static public String NAME = Type
			.getInternalName(ObjectSerializer.class);
	final static public String DESC = Type
			.getDescriptor(ObjectSerializer.class);

	final static public boolean COMPRESS = Boolean.parseBoolean(System
			.getProperty(Defaults._SER_COMPRESS, Defaults.SER_COMPRESS));

	// TODO put this generic. system property
	public static final Serializer SER = new JavaSerializer();

	/**
	 * Serializes the object given by parameter into an array of bytes.
	 * 
	 * @param obj - the object to be serialized.
	 * @return the object serialized into an array of bytes.
	 */
	public static byte[] object2ByteArray(Object obj)
	{
		long serStart = System.nanoTime();
		byte[] payload = SER.object2ByteArray(obj);
		long serEnd = System.nanoTime();
		Profiler.onSerializationFinish(serEnd - serStart);
		return payload;
	}

	/**
	 * De-serializes an array of bytes into the corresponding object. This
	 * returns a new object.
	 * 
	 * @param array - the array of bytes to be de-serialized.
	 * @return the object de-serialized from an array of bytes.
	 * @throws GCPayloadException
	 */
	public static Object byteArray2Object(byte[] array)
			throws GCPayloadException
	{
		return SER.byteArray2Object(array);
	}

	public static final String WRITE_METHOD_NAME = "writeReplaceHook";
	public static final String WRITE_METHOD_DESC = "(" + UniqueObject.DESC
			+ ")" + Type.getDescriptor(Object.class);

	/**
	 * This method is used by every UniqueObject when the corresponding
	 * writeReplace method is called by the Java serialization feature.
	 * 
	 * @param obj - the object to be serialized.
	 * @return the corresponding object.
	 * @throws ObjectStreamException
	 */
	public abstract Object writeReplaceHook(UniqueObject obj)
			throws ObjectStreamException;

	public static final String READ_METHOD_NAME = "readResolveHook";
	public static final String READ_METHOD_DESC = "(" + UniqueObject.DESC + ")"
			+ Type.getDescriptor(Object.class);

	/**
	 * This method is used by every UniqueObject when the corresponding
	 * readResolve method is called by the Java serialization feature.
	 * 
	 * @param obj - the object to be de-serialized.
	 * @return the corresponding object.
	 * @throws ObjectStreamException
	 */
	public abstract Object readResolveHook(UniqueObject obj)
			throws ObjectStreamException;
}
