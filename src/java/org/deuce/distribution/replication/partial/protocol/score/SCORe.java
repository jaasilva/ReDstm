package org.deuce.distribution.replication.partial.protocol.score;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

import org.apache.log4j.Logger;
import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.PartialReplicationProtocol;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.score.SCOReContext;
import org.deuce.transaction.score.SCOReContextState;
import org.deuce.transaction.score.SCOReReadSet;
import org.deuce.transaction.score.SCOReWriteSet;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCORe extends PartialReplicationProtocol implements
		DeliverySubscriber
{
	private static final Logger LOGGER = Logger.getLogger(SCORe.class);
	private final Map<Integer, DistributedContext> contexts = Collections
			.synchronizedMap(new HashMap<Integer, DistributedContext>());
	/**
	 * Queue of pending committing transactions ordered by SnapshotId
	 */
	// CHECKME deve ser static?
	private final Queue<PendingTx> pendQ = new PriorityQueue<PendingTx>();

	// static final Queue<PendingTx> pendQ = new
	// PriorityBlockingQueue<PendingTx>();

	// CHECKME deve ser static?
	private final Queue<PendingTx> stableQ = new PriorityQueue<PendingTx>();

	// static final Queue<PendingTx> stableQ = new
	// PriorityBlockingQueue<PendingTx>();

	private List<Integer> proposedSn;
	private List<VoteMessage> votes;

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.DistributedProtocol#init()
	 */
	@Override
	public void init()
	{
		TribuDSTM.subscribeDeliveries(this);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.DistributedProtocol#onTxContextCreation(org.deuce
	 * .transaction.DistributedContext)
	 */
	@Override
	public void onTxContextCreation(DistributedContext ctx)
	{
		contexts.put(ctx.threadID, ctx);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.DistributedProtocol#onTxBegin(org.deuce.transaction
	 * .DistributedContext)
	 */
	@Override
	public void onTxBegin(DistributedContext ctx)
	{
		// TODO onTxBegin SCORe
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.DistributedProtocol#onTxCommit(org.deuce.transaction
	 * .DistributedContext)
	 */
	@Override
	public void onTxCommit(DistributedContext ctx)
	{
		proposedSn = new LinkedList<Integer>();
		votes = new LinkedList<VoteMessage>();
		
		DistributedContextState ctxState = ctx.createState();
		byte[] payload = ObjectSerializer.object2ByteArray(ctxState);

		Group group1 = ((SCOReReadSet) ctxState.rs).getInvolvedNodes();
		Group group2 = ((SCOReWriteSet) ctxState.ws).getInvolvedNodes();

		// CHECKME guardar tx. sou o coordenador deste commit. esperar timeout
		// segundos por group1.length + group2.length vote messages. caso
		// contrario abort
		TribuDSTM.sendTotalOrdered(payload, group1, group2);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.DistributedProtocol#onTxFinished(org.deuce.transaction
	 * .DistributedContext, boolean)
	 */
	@Override
	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
		// TODO onTxFinished SCORe
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.DistributedProtocol#onTxRead(org.deuce.transaction
	 * .DistributedContext, org.deuce.distribution.ObjectMetadata)
	 */
	@Override
	public Object onTxRead(DistributedContext ctx, ObjectMetadata metadata)
	{
		// TODO onTxRead SCORe
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.DistributedProtocol#onTxWrite(org.deuce.transaction
	 * .DistributedContext, org.deuce.distribution.ObjectMetadata,
	 * org.deuce.distribution.UniqueObject)
	 */
	@Override
	public void onTxWrite(DistributedContext ctx, ObjectMetadata metadata,
			UniqueObject obj)
	{
		// TODO onTxWrite SCORe
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber#onDelivery
	 * (java.lang.Object, org.deuce.distribution.groupcomm.Address, int)
	 */
	@Override
	public void onDelivery(Object obj, Address src, int size)
	{
		// TODO onDelivery SCORe
		if (obj instanceof DistributedContextState) // Prepare Message
		{
			prepareMessage(obj, src);
		}
		else if (obj instanceof VoteMessage) // VoteMessage
		{ // this is the coordinator
			voteMessage(obj, src);
		}
		else if (obj instanceof DecideMessage) // Decide Message
		{
			decideMessage(obj, src);
		}
		else if (obj instanceof ReadRequest) // Read Requested
		{
			// TODO stuff
		}
		else if (obj instanceof ReadReturn) // Read Returned
		{
			// TODO stuff
		}

		processTx();
	}

	private void prepareMessage(Object obj, Address src)
	{
		SCOReContextState ctxState = (SCOReContextState) obj;
		boolean outcome = false;

		// TODO stuff

		if (outcome)
		{
			int nextId = SCOReContext.nextId.incrementAndGet();
			PendingTx pend = new PendingTx(ctxState.ctxID, ctxState.sid, src);
			pendQ.add(pend);

			VoteMessage vote = new VoteMessage(ctxState.ctxID, outcome, nextId,
					src);
			byte[] payload = ObjectSerializer.object2ByteArray(vote);
			TribuDSTM.sendTo(payload, src);
		}
	}

	private void voteMessage(Object obj, Address src)
	{
		VoteMessage vote = (VoteMessage) obj;

		// TODO stuff

		if (vote.result)
		{
			// guardar proposedTimestamp
		}

		// depois de ter todos os votes YES
		int finalSid = 0; // Collections.max(arg0);

		// CHECKME result (true) in args. mudar
		DecideMessage decide = new DecideMessage(vote.ctxID, finalSid, true,
				src);
		// TODO stuff enviar decide messages
	}

	private void decideMessage(Object obj, Address src)
	{
		DecideMessage decide = (DecideMessage) obj;

		if (decide.result)
		{
			SCOReContext.nextId.set(Math.max(SCOReContext.nextId.get(),
					decide.finalSid));
			stableQ.add(new PendingTx(decide.ctxID, decide.finalSid, src));
		}

		pendQ.remove(new PendingTx(decide.ctxID, 0, src));

		if (!decide.result)
		{
			// TODO release locks

			if (src.isLocal())
			{
				// TODO stuff
			}
			else
			{
				// TODO stuff
			}
		}
	}

	private void processTx()
	{
		boolean keepProcessing = true;
		while (keepProcessing)
		{
			keepProcessing = false;
			if (stableQ.isEmpty())
			{
				return;
			}

			PendingTx sTx = stableQ.peek();
			PendingTx pTx = pendQ.peek();

			if (pTx.sid < sTx.sid)
			{
				return;
			}

			// TODO fazer isto tudo atomicamente

			// TODO apply(sTx.ws, sTx.sid)
			// TODO release locks

			stableQ.poll(); // remove sTx

			if (sTx.addr.isLocal())
			{
				// TODO stuff
			}
		}
	}
}
