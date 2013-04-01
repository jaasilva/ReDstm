package org.deuce.distribution.replication.partial.protocol.score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.PartialReplicationProtocol;
import org.deuce.transaction.ContextDelegator;
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

	private Comparator<Pair<String, Integer>> comp = new Comparator<Pair<String, Integer>>()
	{
		@Override
		public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2)
		{
			return o1.second - o2.second;
		}
	};

	// CHECKME verificar se precisam de ser atomic...
	/**
	 * Maintains the timestamp that was attributed to the last update
	 * transaction to have committed on this node
	 */
	private final AtomicInteger commitId = new AtomicInteger(0);
	/**
	 * Keeps track of the next timestamp that this node will propose when it
	 * will receive a commit request for a transaction that accessed some of the
	 * data that it maintains
	 */
	private final AtomicInteger nextId = new AtomicInteger(0);
	/**
	 * Map<ctxID, ctx> Keeps track of existing distributed contexts
	 */
	private final Map<Integer, DistributedContext> contexts = Collections
			.synchronizedMap(new HashMap<Integer, DistributedContext>());
	/**
	 * Map<ctxID, List<proposedSn>> Keeps track of proposed sid's for each
	 * context, during vote phase
	 */
	private final Map<Integer, List<Integer>> votes = Collections
			.synchronizedMap(new HashMap<Integer, List<Integer>>());
	/**
	 * Map<ctxID, expectedVotes> Keeps track of the expected number of votes to
	 * receive by this coordinator, in the vote phase
	 */
	private final Map<Integer, Integer> expectedVotes = Collections
			.synchronizedMap(new HashMap<Integer, Integer>());
	/**
	 * Map<ctxID, TimerTask> Keeps track of each context timer task, to be able
	 * to cancel the task if the expected number of votes is reached
	 */
	private final Map<Integer, TimerTask> timeoutTasks = Collections
			.synchronizedMap(new HashMap<Integer, TimerTask>());
	/**
	 * Queue of pending committing transactions ordered by SnapshotId
	 */
	private final Queue<Pair<String, Integer>> pendQ = new PriorityQueue<Pair<String, Integer>>(
			11, comp);
	/**
	 * 
	 */
	private final Queue<Pair<String, Integer>> stableQ = new PriorityQueue<Pair<String, Integer>>(
			11, comp);
	/**
	 * 
	 */
	private final Map<String, DistributedContextState> receivedTrxs = Collections
			.synchronizedMap(new HashMap<String, DistributedContextState>());

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
		int ctxID = ctx.threadID;
		DistributedContextState ctxState = ctx.createState();
		byte[] payload = ObjectSerializer.object2ByteArray(ctxState);

		Group resGroup = ((SCOReContext) ctx).getInvolvedNodes();
		int expVotes = resGroup.getSize();

		votes.put(ctxID, new ArrayList<Integer>(expVotes));
		expectedVotes.put(ctxID, expVotes);

		// send prepare message
		TribuDSTM.sendTotalOrdered(payload, resGroup);

		TimerTask task = voteTimeoutHandler(ctxID, ((SCOReContext) ctx).trxID);
		timeoutTasks.put(ctxID, task); // set timeout handler
		timeoutTimer.schedule(task, timeout);

		LOGGER.trace(((SCOReContextState) ctxState).origin
				+ " trying to commit " + ctx.threadID + ":" + ctx.atomicBlockId
				+ ":" + ((SCOReContext) ctx).trxID
				+ " --- sending prepare msg to " + resGroup);
	}

	private TimerTask voteTimeoutHandler(final int id, final String trxid)
	{
		return new TimerTask()
		{
			private final int ctxID = id;
			private final String trxID = trxid;

			public void run()
			{ // just to double check if it is really necessary to run this
				if (votes.get(ctxID).size() != expectedVotes.get(ctxID))
				{ // timeout occurs. abort transaction
					finalizeVoteStep(ctxID, trxID, false);
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
			readRequest(obj);
		}
		else if (obj instanceof ReadReturn) // Read Returned
		{ // I requested this object
			readReturn(obj);
		}
		processTx();
	}

	private void prepareMessage(Object obj, Address src)
	{
		SCOReContextState ctxState = (SCOReContextState) obj;
		receivedTrxs.put(ctxState.trxID, ctxState);

		// TODO get locks and validate rs. save result in outcome
		boolean outcome = false;

		int next = 0;
		if (outcome) // valid trx
		{
			next = nextId.incrementAndGet();
			pendQ.add(new Pair<String, Integer>(ctxState.trxID, next));
		}

		LOGGER.trace("Received prepare msg from " + ctxState.origin
				+ " for trx " + ctxState.ctxID + ":" + ctxState.atomicBlockId
				+ ":" + ctxState.trxID);

		VoteMessage vote = new VoteMessage(ctxState.ctxID, outcome, next,
				ctxState.trxID);
		byte[] payload = ObjectSerializer.object2ByteArray(vote);
		TribuDSTM.sendTo(payload, src); // send vote message
	}

	private void voteMessage(Object obj)
	{
		VoteMessage vote = (VoteMessage) obj;
		int ctxID = vote.ctxID;
		String trxID = vote.trxID;

		if (!receivedTrxs.containsKey(trxID))
		{ // late vote. trx already aborted and finished
			return;
		}

		if (!vote.result) // voted NO
		{ // do not wait for more votes. abort trx
			timeoutTasks.get(ctxID).cancel(); // cancel timeout
			finalizeVoteStep(ctxID, trxID, false);
		}
		else
		{ // voted YES. save proposed timestamp
			votes.get(ctxID).add(vote.proposedTimestamp);

			if (votes.get(ctxID).size() == expectedVotes.get(ctxID))
			{ // last vote. every vote was YES. send decide message
				timeoutTasks.get(ctxID).cancel(); // cancel timeout
				finalizeVoteStep(ctxID, trxID, true);
			}
		}
	}

	private void finalizeVoteStep(int ctxID, String trxID, boolean outcome)
	{
		int finalSid = Collections.max(votes.get(ctxID));
		((SCOReContext) contexts.get(ctxID)).sid = finalSid;

		DecideMessage decide = new DecideMessage(ctxID, trxID, finalSid,
				outcome);
		Group group = ((SCOReContext) contexts.get(ctxID)).getInvolvedNodes();

		byte[] payload = ObjectSerializer.object2ByteArray(decide);
		TribuDSTM.sendTotalOrdered(payload, group); // send decide message
	}

	private void decideMessage(Object obj, Address src)
	{
		DecideMessage decide = (DecideMessage) obj;

		if (decide.result)
		{
			int max = Math.max(nextId.get(), decide.finalSid);
			nextId.set(max);

			stableQ.add(new Pair<String, Integer>(decide.trxID, decide.finalSid));
		}

		pendQ.remove(new Pair<String, Integer>(decide.trxID, 0));

		if (!decide.result)
		{
			// TODO release locks

			if (src.isLocal())
			{
				DistributedContext ctx = contexts.get(decide.ctxID);
				ctx.processed(false);
			}
			receivedTrxs.remove(decide.trxID);
		}
	}

	private void readRequest(Object obj)
	{
		// TODO implementar readRequest
	}

	private void readReturn(Object obj)
	{
		// TODO implementar readReturn
	}

	private void processTx()
	{
		while (true)
		{
			if (stableQ.isEmpty())
			{ // nothing to do
				return;
			}

			Pair<String, Integer> sTx = stableQ.peek();
			Pair<String, Integer> pTx = pendQ.peek();

			if (pTx != null && pTx.second < sTx.second)
			{ // there are still some trxs that can be serialized before sTx
				return;
			}

			DistributedContext ctx = null;
			SCOReContextState tx = (SCOReContextState) receivedTrxs
					.get(sTx.first);

			if (tx.origin.isLocal())
			{
				ctx = contexts.get(tx.ctxID);
			}
			else
			{
				ctx = (DistributedContext) ContextDelegator.getInstance();
				ctx.recreateContextFromState(tx);
			}

			ctx.applyWriteSet();

			// TODO release locks

			stableQ.poll(); // remove sTx
			receivedTrxs.remove(sTx.first);

			ctx.processed(true);
		}
	}
}
