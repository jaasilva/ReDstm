package org.deuce.distribution.replication.partial;

import java.io.ObjectStreamException;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.UniqueObject;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class PartialReplicationSerializer extends ObjectSerializer
{
	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.ObjectSerializer#writeReplaceHook(org.deuce.
	 * distribution.UniqueObject)
	 */
	@Override
	public Object writeReplaceHook(UniqueObject obj)
			throws ObjectStreamException
	{
		// TODO writeReplaceHook PartialReplicationSerializer
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.ObjectSerializer#readResolveHook(org.deuce.
	 * distribution.UniqueObject)
	 */
	@Override
	public Object readResolveHook(UniqueObject obj)
			throws ObjectStreamException
	{
		// TODO readResolveHook PartialReplicationSerializer
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.ObjectSerializer#createMetadata()
	 */
	@Override
	public ObjectMetadata createMetadata()
	{
		// TODO createMetadata PartialReplicationSerializer
		return null;
	}

}
