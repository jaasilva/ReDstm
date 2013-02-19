package org.deuce.distribution.replication.partial;

import org.deuce.distribution.DistributedProtocol;
import org.deuce.distribution.ObjectSerializer;

/**
 * @author jaasilva
 * 
 */
public abstract class PartialReplicationProtocol implements DistributedProtocol
{

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.DistributedProtocol#getObjectSerializer()
	 */
	@Override
	public ObjectSerializer getObjectSerializer()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
