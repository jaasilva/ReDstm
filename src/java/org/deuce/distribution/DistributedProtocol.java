package org.deuce.distribution;

import org.deuce.distribution.replication.Bootstrap;
import org.deuce.objectweb.asm.Type;
import org.deuce.transaction.DistributedContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

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

	public Object onTxRead(DistributedContext ctx, TxField field);

	public ObjectSerializer getObjectSerializer();
}
