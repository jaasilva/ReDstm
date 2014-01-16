package org.deuce.distribution.replication.full.protocol.nonvoting;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.full.FullReplicationProtocol;
import org.deuce.distribution.replication.group.PartialReplicationGroup;
import org.deuce.profiling.Profiler;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class NonVotingTOA extends FullReplicationProtocol implements
		DeliverySubscriber
{
	private static final Logger LOGGER = Logger.getLogger(NonVotingTOA.class);

	private final Map<Integer, DistributedContext> ctxs = new ConcurrentHashMap<Integer, DistributedContext>();

	public void init()
	{
		TribuDSTM.subscribeDeliveries(this);
	}

	public void onDelivery(Object obj, Address src, int payloadSize)
	{
		Profiler.newMsgRecv(payloadSize);
		DistributedContextState ctxState = (DistributedContextState) obj;
		DistributedContext ctx = null;

		if (src.isLocal())
		{ // local context
			Profiler.onLastVoteReceived(ctxState.ctxID);
			ctx = ctxs.get(ctxState.ctxID);
		}
		else
		{ // remote context
			ctx = (DistributedContext) ContextDelegator.getInstance();
			ctx.recreateContextFromState(ctxState);
		}

		if (ctx.validate())
		{
			ctx.applyWriteSet();
			if (src.isLocal())
			{
				Profiler.onTxDistCommitFinish(ctxState.ctxID);
			}
			ctx.processed(true);

			LOGGER.debug(src + ":" + ctxState.ctxID + ":"
					+ ctxState.atomicBlockId + " committed.");
		}
		else
		{
			if (src.isLocal())
			{
				Profiler.onTxDistCommitFinish(ctxState.ctxID);
			}
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
		Profiler.onTxDistCommitBegin(ctx.threadID);
		Profiler.onSerializationBegin(ctx.threadID);
		byte[] payload = ObjectSerializer.object2ByteArray(ctx.createState());
		Profiler.onSerializationFinish(ctx.threadID);

		Profiler.newMsgSent(payload.length);
		Profiler.onPrepSend(ctx.threadID);
		TribuDSTM.sendTotalOrdered(payload, new PartialReplicationGroup(
				TribuDSTM.ALL));
	}

	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
	}

	public void onTxContextCreation(DistributedContext ctx)
	{
		ctxs.put(ctx.threadID, ctx);
	}

	public Object onTxRead(DistributedContext ctx, TxField field)
	{
		return null;
	}
}
