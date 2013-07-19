package org.deuce.distribution.replication.partial.protocol.voting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.partial.PartialReplicationProtocol;
import org.deuce.transaction.DistributedContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class Voting extends PartialReplicationProtocol implements
		DeliverySubscriber
{
	private static final Logger LOGGER = Logger.getLogger(Voting.class);

	private final Map<Integer, DistributedContext> ctxs = new ConcurrentHashMap<Integer, DistributedContext>();

	@Override
	public void init()
	{
		TribuDSTM.subscribeDeliveries(this);
	}

	@Override
	public void onTxContextCreation(DistributedContext ctx)
	{
		ctxs.put(ctx.threadID, ctx);
	}

	@Override
	public void onTxBegin(DistributedContext ctx)
	{
	}

	@Override
	public void onTxCommit(DistributedContext ctx)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
	}

	@Override
	public Object onTxRead(DistributedContext ctx, TxField field)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDelivery(Object obj, Address src, int size)
	{
		// TODO Auto-generated method stub

	}
}
