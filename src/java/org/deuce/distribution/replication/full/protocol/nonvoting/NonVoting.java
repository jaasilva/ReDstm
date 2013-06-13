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
import org.deuce.profiling.PRProfiler;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class NonVoting extends FullReplicationProtocol implements
		DeliverySubscriber
{
	private static final Logger LOGGER = Logger.getLogger(NonVoting.class);

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
			PRProfiler.onLastVoteReceived(ctxState.ctxID);
			ctx.profiler.newMsgRecv(payloadSize);
			PRProfiler.newMsgRecv(payloadSize);
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
		PRProfiler.onSerializationBegin(ctx.threadID);

		byte[] payload = ObjectSerializer.object2ByteArray(ctx.createState());

		PRProfiler.onSerializationFinish(ctx.threadID);

		ctx.profiler.onTOSend();
		PRProfiler.onPrepSend(ctx.threadID);
		ctx.profiler.newMsgSent(payload.length);
		PRProfiler.newMsgSent(payload.length);

		TribuDSTM.sendTotalOrdered(payload);
	}

	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
	}

	public void onTxContextCreation(DistributedContext ctx)
	{
		contexts.put(ctx.threadID, ctx);
	}

	public Object onTxRead(DistributedContext ctx, ObjectMetadata metadata)
	{
		return null;
	}
}
