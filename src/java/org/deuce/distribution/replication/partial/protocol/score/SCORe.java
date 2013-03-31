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
import org.deuce.distribution.replication.group.GroupUtils;
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
		int id = ctx.threadID;
		DistributedContextState ctxState = ctx.createState();
		byte[] payload = ObjectSerializer.object2ByteArray(ctxState);

		Group group1 = ((SCOReReadSet) ctxState.rs).getInvolvedNodes();
		Group group2 = ((SCOReWriteSet) ctxState.ws).getInvolvedNodes();
		Group resGroup = GroupUtils.unionGroups(group1, group2);
		int expVotes = resGroup.getSize();

		votes.put(id, new ArrayList<Integer>(expVotes));
		expectedVotes.put(id, expVotes);

		// send prepare message
		TribuDSTM.sendTotalOrdered(payload, resGroup);

		TimerTask task = voteTimeoutHandler(id); // set timeout handler
		timeoutTasks.put(id, task);
		timeoutTimer.schedule(task, timeout);
	}

	private TimerTask voteTimeoutHandler(final int id)
	{
		return new TimerTask()
		{
			private final int ctxID = id;

			public void run()
			{ // XXX ver se este if é necessario...
				if (votes.get(id).size() != expectedVotes.get(id))
				{ // timeout occurs. abort transaction
					// TODO
				}

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
	public Object onTxRead(DistributedContext ctx, ObjectMetadata metadata,
			Object value)
	{
		return null; // TODO onTxRead SCORe
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
		else if (obj instanceof VoteMessage) // Vote Message
		{ // I am the coordinator of this commit
			voteMessage(obj);
		}
		else if (obj instanceof DecideMessage) // Decide Message
		{
			decideMessage(obj, src);
		}
		else if (obj instanceof ReadRequest) // Read Requested
		{ // I have the requested object
			// TODO implementar readRequest
		}
		else if (obj instanceof ReadReturn) // Read Returned
		{ // I requested this object
			// TODO implementar readReturn
		}
		processTx();
	}

	private void prepareMessage(Object obj, Address src)
	{
		SCOReContextState ctxState = (SCOReContextState) obj;
		boolean outcome = false;

		// TODO get locks and validate rs. save result in outcome

		if (outcome) // valid transaction
		{
			ctxState.sid = SCOReContext.nextId.incrementAndGet();

			if (src.isLocal())
			{ // XXX é mesmo necessario?
				((SCOReContext) contexts.get(ctxState.ctxID)).sid = ctxState.sid;
			}

			PendingTx pend = new PendingTx(ctxState, src);
			pendQ.add(pend);
		}

		VoteMessage vote = new VoteMessage(ctxState.ctxID, outcome,
				ctxState.sid);
		byte[] payload = ObjectSerializer.object2ByteArray(vote);
		TribuDSTM.sendTo(payload, src); // send vote message
	}

	private void voteMessage(Object obj)
	{
		VoteMessage vote = (VoteMessage) obj;
		boolean outcome = true;

		// CHECKME verificar votos para transacoes já abortadas... votos
		// atrasados

		if (!vote.result) // voted NO
		{
			outcome = false;
			finalizeVoteStep(vote.ctxID, outcome);
		}
		else
		{ // voted YES. save proposed timestamp
			votes.get(vote.ctxID).add(vote.proposedTimestamp);

			if (votes.get(vote.ctxID).size() == expectedVotes.get(vote.ctxID))
			{ // last vote. every vote was YES. send decide message
				finalizeVoteStep(vote.ctxID, outcome);
			}
		}
	}

	private void finalizeVoteStep(int ctxID, boolean outcome)
	{
		timeoutTasks.get(ctxID).cancel(); // cancel timeout
		int finalSid = Collections.max(votes.get(ctxID));
		((SCOReContext) contexts.get(ctxID)).sid = finalSid;

		DecideMessage decide = new DecideMessage(ctxID, finalSid, outcome);

		// TODO send decide message
	}

	private void decideMessage(Object obj, Address src)
	{
		DecideMessage decide = (DecideMessage) obj;

		if (decide.result)
		{
			int max = Math.max(SCOReContext.nextId.get(), decide.finalSid);
			SCOReContext.nextId.set(max);
			
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
