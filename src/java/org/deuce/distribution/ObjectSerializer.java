package org.deuce.distribution;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.deuce.Defaults;
import org.deuce.distribution.serialization.GCPayloadException;
import org.deuce.objectweb.asm.Type;
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

	/**
	 * Serializes the object given by parameter into an array of bytes.
	 * 
	 * @param obj - the object to be serialized.
	 * @return the object serialized into an array of bytes.
	 */
	public static byte[] object2ByteArray(Object obj)
	{
		byte[] payload = null;
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedOutputStream bos = null;
			GZIPOutputStream gzos = null;
			ObjectOutputStream oos;
			if (COMPRESS)
			{
				gzos = new GZIPOutputStream(baos);
				bos = new BufferedOutputStream(gzos);
				oos = new ObjectOutputStream(bos);
			}
			else
			{
				oos = new ObjectOutputStream(baos);
			}
			oos.writeObject(obj);
			oos.flush();
			oos.close();
			if (COMPRESS)
			{
				bos.close();
				gzos.close();
			}
			baos.close();
			payload = baos.toByteArray();
		}
		catch (Exception e)
		{
			System.err.println("Could not serialize object.");
			e.printStackTrace();
			System.exit(-1);
		}
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
		Object obj = null;
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(array);
			BufferedInputStream bis = null;
			GZIPInputStream gzis = null;
			ObjectInputStream ois = null;
			if (COMPRESS)
			{
				gzis = new GZIPInputStream(bais);
				bis = new BufferedInputStream(gzis);
				ois = new ObjectInputStream(bis);
			}
			else
			{
				ois = new ObjectInputStream(bais);
			}
			try
			{
				obj = ois.readObject();
			}
			catch (NullPointerException e)
			{
				e.printStackTrace();
				throw new GCPayloadException();
			}
			catch (ClassNotFoundException e)
			{
				System.err.println("Could not deserialize object.");
				e.printStackTrace();
				System.exit(-1);
			}
			finally
			{
				ois.close();
				if (COMPRESS)
				{
					bis.close();
					gzis.close();
				}
				bais.close();
			}
		}
		catch (IOException e)
		{
			System.err.println("Could not deserialize object.");
			e.printStackTrace();
			System.exit(-1);
		}
		return obj;
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

	/**
	 * Creates an empty metadata object.
	 * 
	 * @return a new metadata object.
	 */
	public abstract ObjectMetadata createMetadata();
}
