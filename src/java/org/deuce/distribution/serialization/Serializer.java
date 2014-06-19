package org.deuce.distribution.serialization;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public abstract class Serializer
{
	public abstract byte[] object2ByteArray(Object obj);

	public abstract Object byteArray2Object(byte[] array)
			throws GCPayloadException;
}
