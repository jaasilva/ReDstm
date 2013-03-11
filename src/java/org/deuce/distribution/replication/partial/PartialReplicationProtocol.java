package org.deuce.distribution.replication.partial;

import org.deuce.distribution.DistributedProtocol;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public abstract class PartialReplicationProtocol implements DistributedProtocol
{
	public static final String PARTIALREP_DESC = Type
			.getDescriptor(Partial.class);

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.DistributedProtocol#getObjectSerializer()
	 */
	@Override
	public ObjectSerializer getObjectSerializer()
	{
		// TODO getObjectSerializer PartialReplicationProtocol
		return null;
	}

}
