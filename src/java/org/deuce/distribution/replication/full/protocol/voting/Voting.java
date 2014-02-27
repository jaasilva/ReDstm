package org.deuce.distribution.replication.full.protocol.voting;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.full.FullReplicationProtocol;
import org.deuce.distribution.replication.full.protocol.voting.msgs.PendingResult;
import org.deuce.distribution.replication.full.protocol.voting.msgs.PendingTx;
import org.deuce.distribution.replication.full.protocol.voting.msgs.ResultMessage;
import org.deuce.profiling.Profiler;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class Voting extends FullReplicationProtocol implements
		DeliverySubscriber
{
	public static final Logger LOGGER = Logger.getLogger(Voting.class);

	private final Map<Integer, DistributedContext> ctxs = new ConcurrentHashMap<Integer, DistributedContext>();
	private final List<PendingTx> pendingTxs = new LinkedList<PendingTx>();
	private final List<PendingResult> pendingResults = new LinkedList<PendingResult>();

	@Override
	public void init()
	{
		TribuDSTM.subscribeDeliveries(this);
	}

	@Override
	public void onDelivery(Object obj, Address src, int payloadSize)
	{
		Profiler.newMsgRecv(payloadSize);

		if (obj instanceof DistributedContextState)
		{ // WS message
			DistributedContextState ctxState = (DistributedContextState) obj;
			PendingTx tx = new PendingTx(src, ctxState);

			if (src.isLocal())
			{ // local context
				Profiler.onLastVoteReceived(ctxState.ctxID);
			}

			// Check for existing result
			PendingResult pendingResult = null;
			boolean exists = false;
			for (PendingResult res : pendingResults)
			{
				if (res.src.equals(src) && res.msg.ctxID == ctxState.ctxID)
				{
					exists = true;
					pendingResult = res;
					break;
				}
			}

			if (exists)
			{ // If it exists, set the tx state
				tx.result = pendingResult.msg.result ? PendingTx.COMMITTED
						: PendingTx.ABORTED;
				pendingResults.remove(pendingResult);
			}
			pendingTxs.add(tx);
		}
		else if (obj instanceof ResultMessage)
		{ // Result message
			ResultMessage msg = (ResultMessage) obj;

			// Check for existing tx
			PendingTx pendingTx = null;
			boolean exists = false;
			for (PendingTx tx : pendingTxs)
			{
				if (tx.src.equals(src) && tx.ctxState.ctxID == msg.ctxID)
				{
					exists = true;
					pendingTx = tx;
					break;
				}
			}
			// If it exists, set the tx state
			if (exists)
			{
				pendingTx.result = msg.result ? PendingTx.COMMITTED
						: PendingTx.ABORTED;
			}
			else
			{
				pendingResults.add(new PendingResult(src, msg));
			}
		}
		processTx();
	}

	private void processTx()
	{
		boolean keepProcessing = true;
		while (keepProcessing)
		{
			keepProcessing = false;
			if (pendingTxs.isEmpty())
				return;

			PendingTx tx = pendingTxs.get(0);
			if (!tx.src.isLocal() && tx.result == PendingTx.WAITING)
				return;

			DistributedContext ctx = null;
			// If tx's result has been received, apply it
			// If not, and tx is local, validate and bcast result
			if (tx.result > PendingTx.VALIDATED)
			{ // Result already known
				pendingTxs.remove(0);

				if (tx.src.isLocal())
				{
					ctx = ctxs.get(tx.ctxState.ctxID);
				}
				else
				{
					ctx = (DistributedContext) ContextDelegator.getInstance();
					ctx.recreateContextFromState(tx.ctxState);
				}

				if (tx.result == PendingTx.COMMITTED)
				{
					ctx.applyWriteSet();
					ctx.processed(true);

					LOGGER.debug(tx.src + ":" + tx.ctxState.ctxID + ":"
							+ tx.ctxState.atomicBlockId + " committed.");
				}
				else
				{
					ctx.processed(false);

					LOGGER.debug(tx.src + ":" + tx.ctxState.ctxID + ":"
							+ tx.ctxState.atomicBlockId + " aborted.");
				}
				keepProcessing = true;
			}
			else if (tx.src.isLocal() && tx.result == PendingTx.WAITING)
			{ // Validate tx
				ctx = ctxs.get(tx.ctxState.ctxID);
				boolean valid = ctx.validate();
				tx.result = PendingTx.VALIDATED;

				Profiler.onSerializationBegin(ctx.threadID);
				byte[] payload = ObjectSerializer
						.object2ByteArray(new ResultMessage(tx.ctxState.ctxID,
								valid));
				Profiler.onSerializationFinish(ctx.threadID);

				Profiler.newMsgSent(payload.length);
				TribuDSTM.sendReliably(payload);
			}
		}
	}

	@Override
	public void onTxBegin(DistributedContext ctx)
	{
	}

	@Override
	public void onTxCommit(DistributedContext ctx)
	{
		DistributedContextState ctxState = ctx.createState();
		ctxState.rs = null; // cleaning readSet

		Profiler.onSerializationBegin(ctx.threadID);
		byte[] payload = ObjectSerializer.object2ByteArray(ctxState);
		Profiler.onSerializationFinish(ctx.threadID);

		Profiler.newMsgSent(payload.length);
		Profiler.onPrepSend(ctx.threadID);
		TribuDSTM.sendTotalOrdered(payload);
	}

	@Override
	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
	}

	@Override
	public void onTxContextCreation(DistributedContext ctx)
	{
		ctxs.put(ctx.threadID, ctx);
	}

	@Override
	public Object onTxRead(DistributedContext ctx, TxField field)
	{
		return null;
	}
}
