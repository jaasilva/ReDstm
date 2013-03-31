package org.deuce.distribution;

import org.deuce.distribution.replication.Bootstrap;
import org.deuce.objectweb.asm.Type;
import org.deuce.transaction.DistributedContext;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public interface DistributedProtocol
{
	public static final String BOOTSTRAP_DESC = Type
			.getDescriptor(Bootstrap.class);
	public static final String BOOTSTRAP_ID_PARAM_NAME = "id";

	public void init();

	public void onTxContextCreation(DistributedContext ctx);

	public void onTxBegin(DistributedContext ctx);

	public void onTxCommit(DistributedContext ctx);

	public void onTxFinished(DistributedContext ctx, boolean committed);

	public ObjectSerializer getObjectSerializer();

	public Object onTxRead(DistributedContext ctx, ObjectMetadata metadata, Object value);

	public void onTxWrite(DistributedContext ctx, ObjectMetadata metadata,
			UniqueObject obj);
}
