package org.deuce.distribution.replication.full;

import org.deuce.distribution.DistributedProtocol;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public abstract class FullReplicationProtocol implements DistributedProtocol
{
	private FullReplicationSerializer serializer = new FullReplicationSerializer();

	@Override
	public ObjectSerializer getObjectSerializer()
	{
		return serializer;
	}
}
