package org.deuce.distribution.serialization;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.deuce.distribution.ObjectSerializer;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class JavaSerializer extends Serializer
{
	@Override
	public byte[] object2ByteArray(Object obj)
	{
		byte[] payload = null;
		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedOutputStream bos = null;
			GZIPOutputStream gzos = null;
			ObjectOutputStream oos;
			if (ObjectSerializer.COMPRESS)
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
			if (ObjectSerializer.COMPRESS)
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

	@Override
	public Object byteArray2Object(byte[] array) throws GCPayloadException
	{
		Object obj = null;
		try
		{
			ByteArrayInputStream bais = new ByteArrayInputStream(array);
			BufferedInputStream bis = null;
			GZIPInputStream gzis = null;
			ObjectInputStream ois = null;

			if (ObjectSerializer.COMPRESS)
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
				if (ObjectSerializer.COMPRESS)
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
}
