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
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class NonVoting extends FullReplicationProtocol implements
		DeliverySubscriber
{
	private static final Logger LOGGER = Logger.getLogger(NonVoting.class);
	// public static final Logger log = Logger.getLogger(NonVoting.class);
	// static {
	// try {
	// log.removeAllAppenders();
	// log.addAppender(new FileAppender(new PatternLayout("%c{1} - %m%n"),
	// System.getProperty("tribu.groupcommunication.group",
	// "tvale")+"id"+Integer.getInteger("tribu.site")+".log", false));
	// log.setLevel(Level.TRACE);
	// } catch (IOException e) {
	// // TODOs Auto-generated catch block
	// e.printStackTrace();
	// }
	// }

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
