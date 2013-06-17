package org.deuce.distribution.replication.full.protocol.nonvoting;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.full.FullReplicationProtocol;
import org.deuce.distribution.replication.group.PartialReplicationGroup;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class NonVotingTOA extends FullReplicationProtocol implements
		DeliverySubscriber
{
	private static final Logger LOGGER = Logger.getLogger(NonVotingTOA.class);
	private final Map<Integer, DistributedContext> contexts = Collections
			.synchronizedMap(new HashMap<Integer, DistributedContext>());

	public void init()
	{
		TribuDSTM.subscribeDeliveries(this);
	}

	public void onDelivery(Object obj, Address src, int payloadSize)
	{
		DistributedContextState ctxState = (DistributedContextState) obj;
		DistributedContext ctx = null;

		if (src.isLocal())
		{
			ctx = contexts.get(ctxState.ctxID);
			ctx.profiler.onTODelivery();
			ctx.profiler.newMsgRecv(payloadSize);
		}
		else
		{
			ctx = (DistributedContext) ContextDelegator.getInstance();
			ctx.recreateContextFromState(ctxState);
		}

		if (ctx.validate())
		{
			ctx.applyWriteSet();
			ctx.processed(true);

			LOGGER.debug(src + ":" + ctxState.ctxID + ":"
					+ ctxState.atomicBlockId + " committed.");
		}
		else
		{
			ctx.processed(false);

			LOGGER.debug(src + ":" + ctxState.ctxID + ":"
					+ ctxState.atomicBlockId + " aborted.");
		}
	}

	public void onTxBegin(DistributedContext ctx)
	{
	}

	public void onTxCommit(DistributedContext ctx)
	{
		byte[] payload = ObjectSerializer.object2ByteArray(ctx.createState());

		ctx.profiler.onTOSend();
		ctx.profiler.newMsgSent(payload.length);

		TribuDSTM.sendTotalOrdered(payload, new PartialReplicationGroup(TribuDSTM.ALL));
	}

	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
	}

	public void onTxContextCreation(DistributedContext ctx)
	{
		contexts.put(ctx.threadID, ctx);
	}

	@Override
	public Object onTxRead(DistributedContext ctx, ObjectMetadata metadata)
	{
		return null;
	}
}
