package org.deuce.distribution.replication.partial.protocol.score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

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
import org.deuce.transaction.score.*;
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
	/**
	 * Map<ctxID, ctx> Keeps track of existing distributed contexts
	 */
	private final Map<Integer, DistributedContext> contexts = Collections
			.synchronizedMap(new HashMap<Integer, DistributedContext>());
	/**
	 * Map<ctxID, List<proposedSn>> Keeps track of proposed sid's for each
	 * context
	 */
	private final Map<Integer, List<Integer>> votes = Collections
			.synchronizedMap(new HashMap<Integer, List<Integer>>());
	/**
	 * Map<ctxID, expectedVotes>
	 */
	private final Map<Integer, Integer> expectedVotes = Collections
			.synchronizedMap(new HashMap<Integer, Integer>());
	/**
	 * Map<ctxID, TimerTask>
	 */
	private final Map<Integer, TimerTask> timeoutTasks = Collections
			.synchronizedMap(new HashMap<Integer, TimerTask>());
	/**
	 * Queue of pending committing transactions ordered by SnapshotId
	 */
	private final Queue<PendingTx> pendQ = new PriorityQueue<PendingTx>();
	/**
	 * 
	 */
	private final Queue<PendingTx> stableQ = new PriorityQueue<PendingTx>();

	private final int timeout = Integer.getInteger(
			"tribu.distributed.protocol.score.timeout", 5000);
	private final Timer timeoutTimer = new Timer();

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
		DistributedContextState ctxState = ctx.createState();
		byte[] payload = ObjectSerializer.object2ByteArray(ctxState);

		Group group1 = ((SCOReReadSet) ctxState.rs).getInvolvedNodes();
		Group group2 = ((SCOReWriteSet) ctxState.ws).getInvolvedNodes();
		int expectedVotes = group1.getSize() + group2.getSize();

		votes.put(ctx.threadID, new ArrayList<Integer>(expectedVotes));
		this.expectedVotes.put(ctx.threadID, expectedVotes);

		// send prepare message
		TribuDSTM.sendTotalOrdered(payload, group1, group2);

		TimerTask task = voteTimeoutHandler(); // set timeout handler
		timeoutTasks.put(ctx.threadID, task);
		timeoutTimer.schedule(task, timeout);
	}

	private TimerTask voteTimeoutHandler()
	{
		return new TimerTask()
		{
			public void run()
			{
				if (proposedSn.size() != expectedVotes)
				{ // timeout occurs. abort transaction
					SCOReContext ctx = (SCOReContext) contexts
							.get(currentCommittingCtxID);

					DecideMessage decide = new DecideMessage(ctx.threadID,
							ctx.sid, false);

					// TODO send decide messages
				}
			}
		};
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
		if (obj instanceof DistributedContextState) // Prepare Message
		{
			prepareMessage(obj, src);
		}
		else if (obj instanceof VoteMessage) // VoteMessage
		{ // this is the coordinator
			voteMessage(obj);
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

		// TODO get locks and validate rs. save result in outcome

		if (outcome)
		{ // valid
			SCOReContext.nextId.incrementAndGet();
			PendingTx pend = new PendingTx(ctxState, src);
			pendQ.add(pend);
		}

		VoteMessage vote = new VoteMessage(ctxState.ctxID, outcome,
				SCOReContext.nextId.get());
		byte[] payload = ObjectSerializer.object2ByteArray(vote);
		TribuDSTM.sendTo(payload, src); // send vote message
	}

	private void voteMessage(Object obj)
	{
		VoteMessage vote = (VoteMessage) obj;
		boolean outcome = true;

		if (vote.ctxID != currentCommittingCtxID) // CHECKME isto nao
													// funciona!!!!!!!!
		{ // discard timed out vote message
			return;
		}

		if (!vote.result) // voted NO
		{
			outcome = false;
			finalizeVoteStep(outcome);
		}
		else
		{ // voted YES. save proposed timestamp
			proposedSn.add(vote.proposedTimestamp);

			if (proposedSn.size() == expectedVotes)
			{ // last vote. every vote was YES. send decide message
				task.cancel(); // cancel timeout
				finalizeVoteStep(outcome);
			}
		}
	}

	private void finalizeVoteStep(boolean outcome)
	{
		int finalSid = Collections.max(proposedSn);
		SCOReContext ctx = (SCOReContext) contexts.get(currentCommittingCtxID);
		ctx.sid = finalSid;
		DecideMessage decide = new DecideMessage(currentCommittingCtxID,
				finalSid, outcome);

		// TODO send decide message
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
				SCOReContext ctx = (SCOReContext) contexts.get(decide.ctxID);
				ctx.processed(false);
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
			// SCOReContext ctx = (SCOReContext) sTx.
			// TODO release locks

			stableQ.poll(); // remove sTx

			if (sTx.src.isLocal())
			{
				// TODO stuff
			}
		}
	}
}
