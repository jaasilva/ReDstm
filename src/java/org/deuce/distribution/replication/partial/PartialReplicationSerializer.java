package org.deuce.distribution.replication.partial;

import java.io.ObjectStreamException;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.UniqueObject;

/**
 * @author jaasilva
 * 
 */
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.ObjectSerializer#createMetadata()
	 */
	@Override
	public ObjectMetadata createMetadata()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
