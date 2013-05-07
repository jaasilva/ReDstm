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

import org.deuce.distribution.serialization.GCPayloadException;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public abstract class ObjectSerializer
{
	final static public String NAME = Type
			.getInternalName(ObjectSerializer.class);
	final static public String DESC = Type
			.getDescriptor(ObjectSerializer.class);

	final static public boolean COMPRESS = Boolean.parseBoolean(System
			.getProperty("tribu.serialization.compress", "true"));

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
		catch (IOException e)
		{
			System.err.println("Could not serialize object.");
			e.printStackTrace();
			System.exit(-1);
		}
		return payload;
	}

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

	public abstract Object writeReplaceHook(UniqueObject obj)
			throws ObjectStreamException;

	public static final String READ_METHOD_NAME = "readResolveHook";
	public static final String READ_METHOD_DESC = "(" + UniqueObject.DESC + ")"
			+ Type.getDescriptor(Object.class);

	public abstract Object readResolveHook(UniqueObject obj)
			throws ObjectStreamException;

	public abstract ObjectMetadata createMetadata();
}
