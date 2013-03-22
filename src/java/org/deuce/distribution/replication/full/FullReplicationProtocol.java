package org.deuce.distribution.replication.full;

import org.deuce.distribution.DistributedProtocol;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.replication.Bootstrap;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public abstract class FullReplicationProtocol implements DistributedProtocol
{
	public static final String BOOTSTRAP_DESC = Type
			.getDescriptor(Bootstrap.class);
	public static final String BOOTSTRAP_ID_PARAM_NAME = "id";

	private FullReplicationSerializer serializer = new FullReplicationSerializer();

	public ObjectSerializer getObjectSerializer()
	{
		return serializer;
	}
}
