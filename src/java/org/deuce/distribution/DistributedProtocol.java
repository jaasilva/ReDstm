package org.deuce.distribution;

import org.deuce.transaction.DistributedContext;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public interface DistributedProtocol
{
	public void init();

	public void onTxContextCreation(DistributedContext ctx);

	public void onTxBegin(DistributedContext ctx);

	public void onTxCommit(DistributedContext ctx);

	public void onTxFinished(DistributedContext ctx, boolean committed);

	public ObjectSerializer getObjectSerializer();

	public Object onTxRead(DistributedContext ctx, ObjectMetadata metadata);

	public void onTxWrite(DistributedContext ctx, ObjectMetadata metadata,
			UniqueObject obj);
}
