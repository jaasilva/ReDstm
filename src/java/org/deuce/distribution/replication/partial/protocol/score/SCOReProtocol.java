package org.deuce.distribution.replication.partial.protocol.score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
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
import org.deuce.distribution.replication.partial.oid.PartialReplicationOID;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.score.InPlaceRWLock;
import org.deuce.transaction.score.SCOReContext;
import org.deuce.transaction.score.SCOReContextState;
import org.deuce.transaction.score.SCOReReadSet;
import org.deuce.transaction.score.SCOReWriteSet;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transaction.score.field.Version;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReProtocol extends PartialReplicationProtocol implements
		DeliverySubscriber
{
	private static final Logger LOGGER = Logger.getLogger(SCOReProtocol.class);

	private Comparator<Pair<String, Integer>> comp = new Comparator<Pair<String, Integer>>()
	{
		@Override
		public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2)
		{
			return o1.second - o2.second;
		}
	};

	private final AtomicInteger commitId = new AtomicInteger(0);
	private final AtomicInteger nextId = new AtomicInteger(0);
	private final AtomicInteger maxSeenId = new AtomicInteger(0);

	private final int timeout = Integer.getInteger(
			"tribu.distributed.protocol.score.timeout", 5000);
	private final Timer timeoutTimer = new Timer();

	private final Map<Integer, DistributedContext> ctxs = Collections
			.synchronizedMap(new HashMap<Integer, DistributedContext>());

	private final Queue<Pair<String, Integer>> pendQ = new PriorityQueue<Pair<String, Integer>>(
			11, comp);
	private final Queue<Pair<String, Integer>> stableQ = new PriorityQueue<Pair<String, Integer>>(
			11, comp);

	private final Map<String, DistributedContextState> receivedTrxs = Collections
			.synchronizedMap(new HashMap<String, DistributedContextState>());
	private final Set<String> rejectTrxs = Collections
			.synchronizedSet(new HashSet<String>());

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
		System.out.println("onTxBegin " + ctx.threadID + ":"
				+ ctx.atomicBlockId);
	}

	@Override
	public void onTxCommit(DistributedContext ctx)
	{ // I am the coordinator of this commit.
		SCOReContext sctx = (SCOReContext) ctx;
		DistributedContextState ctxState = sctx.createState();
		byte[] payload = ObjectSerializer.object2ByteArray(ctxState);

		Group resGroup = sctx.getInvolvedNodes();
		int expVotes = resGroup.size();

		sctx.votes = new ArrayList<Integer>(expVotes);
		sctx.expectedVotes = expVotes;

		TribuDSTM.sendToGroup(payload, resGroup);

		TimerTask task = createTimeoutHandler(sctx);
		sctx.timeoutTask = task;
		timeoutTimer.schedule(task, timeout);

		System.out
				.println(String
						.format("%s trying to commit %d:%s:%d. Sending prep msg to %s. Setting timeout(%s) for %d ms.",
								((SCOReContextState) ctxState).src,
								sctx.threadID, sctx.trxID, sctx.atomicBlockId,
								resGroup, task, timeout));

	}

	private TimerTask createTimeoutHandler(final SCOReContext sctx)
	{ // I am the coordinator of this commit.
		return new TimerTask()
		{
			SCOReContext ctx = sctx;

			public void run()
			{ // just to double check if it is really necessary to run this
				if (ctx.votes.size() != ctx.expectedVotes)
				{ // timeout occurs. abort trx
					System.out
							.println(String
									.format("Timeout for trx %d%s:%d triggered (task(%s)). ABORT TRX.",
											ctx.threadID, ctx.trxID,
											ctx.atomicBlockId, this));

					finalizeVoteStep(ctx, false);
				}
			}
		};
	}

	@Override
	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
	}

	@Override
	public Object onTxRead(DistributedContext ctx, ObjectMetadata metadata)
	{ // I am the coordinator of this read.
		SCOReContext sctx = (SCOReContext) ctx;
		boolean firstRead = !sctx.firstReadDone;
		Group group = ((PartialReplicationOID) metadata).getGroup();

		if (firstRead)
		{ // first read of this transaction
			sctx.sid = commitId.get();
			sctx.firstReadDone = true;
		}

		ReadDone read;
		if (group.contains(TribuDSTM.getLocalAddress()))
		{ // local read
			read = doRead(sctx.sid, metadata);
		}
		else
		{ // remote read
			ReadReq req = new ReadReq(sctx.threadID, metadata, sctx.sid,
					firstRead, sctx.requestVersion);

			byte[] payload = ObjectSerializer.object2ByteArray(req);
			TribuDSTM.sendToGroup(payload, group);

			try
			{ // wait for first response
				sctx.syncMsg.acquire();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			read = sctx.response;
		}

		if (firstRead)
		{ // first read of this transaction
			sctx.sid = read.lastCommitted;
		}

		if (sctx.isUpdate() && !read.mostRecent)
		{
			throw new TransactionException();
		}
		// added to read set in onRead context method
		return read.value;
	}

	private ReadDone doRead(int sid, ObjectMetadata metadata)
	{
		int max = Math.max(nextId.get(), sid);
		nextId.set(max);

		VBoxField field = (VBoxField) TribuDSTM.getObject(metadata);

		while (commitId.get() < sid
				&& !((InPlaceRWLock) field).isExclusiveUnlocked())
		{ // wait until (commitId.get() >= sid || ((InPlaceRWLock)
		} // field).isExclusiveUnlocked()

		Version ver = field.getLastVersion().get(sid);

		boolean mostRecent = ver.equals(field.getLastVersion());
		return new ReadDone(ver.value, commitId.get(), mostRecent);
	}

	private void updateNodeTimestamps(int lastCommitted)
	{
		int max = Math.max(nextId.get(), lastCommitted);
		nextId.set(max);

		int maxSeen = Math.max(maxSeenId.get(), lastCommitted);
		maxSeenId.set(maxSeen);
	}

	private void readRequest(ReadReq msg, Address src)
	{
		int newReadSid = msg.readSid;

		if (msg.firstRead && commitId.get() > msg.readSid)
		{
			newReadSid = commitId.get();
		}

		ReadDone read = doRead(newReadSid, msg.metadata);

		ReadRet ret = new ReadRet(msg.ctxID, msg.msgVersion, read);
		byte[] payload = ObjectSerializer.object2ByteArray(ret);

		TribuDSTM.sendTo(payload, src);
		updateNodeTimestamps(msg.readSid);
	}

	private void readReturn(ReadRet msg, Address src)
	{ // I am the coordinator of this read.
		SCOReContext sctx = ((SCOReContext) ctxs.get(msg.ctxID));

		if (sctx.requestVersion > msg.msgVersion)
		{ // read request already responded. ignore late response
			return; // CHECKME is this enough? need AtomicInteger?
		}
		sctx.response = msg.read;

		updateNodeTimestamps(msg.read.lastCommitted);

		sctx.requestVersion++;
		sctx.syncMsg.release();
	}

	@Override
	public void onDelivery(Object obj, Address src, int size)
	{
		System.out.println("RECEIVED MESSAGE " + obj + " FROM: " + src);
		if (obj instanceof DistributedContextState) // Prepare Message
		{
			prepareMessage((SCOReContextState) obj, src);
		}
		else if (obj instanceof VoteMsg) // Vote Message
		{ // I am the coordinator of this commit
			voteMessage((VoteMsg) obj, src);
		}
		else if (obj instanceof DecideMsg) // Decide Message
		{
			decideMessage((DecideMsg) obj, src);
		}
		else if (obj instanceof ReadReq) // Read Request
		{ // I have the requested object
			readRequest((ReadReq) obj, src);
		}
		else if (obj instanceof ReadRet) // Read Return
		{ // I requested this object
			readReturn((ReadRet) obj, src);
		}
		processTx();
	}

	private void prepareMessage(SCOReContextState ctx, Address src)
	{ // I am a participant in this commit. Validate and send vote msg
		receivedTrxs.put(ctx.trxID, ctx);

		System.out.println(String.format(
				"%s received a prep msg from %s for trx %d:%s.",
				TribuDSTM.getLocalAddress(), src, ctx.ctxID, ctx.trxID));

		boolean outcome = ((SCOReWriteSet) ctx.ws).getExclusiveLocks()
				&& ((SCOReReadSet) ctx.rs).getSharedLocks()
				&& ((SCOReReadSet) ctx.rs).validate(ctx.sid);

		int next = -1;
		if (outcome) // valid trx
		{
			next = nextId.incrementAndGet();
			pendQ.add(new Pair<String, Integer>(ctx.trxID, next));
		}

		VoteMsg vote = new VoteMsg(outcome, next, ctx.ctxID);
		byte[] payload = ObjectSerializer.object2ByteArray(vote);
		TribuDSTM.sendTo(payload, src);

		System.out.println(String.format(
				"%s sending vote msg(res=%s, fsn=%d) to %s.",
				TribuDSTM.getLocalAddress(), outcome, next, src));
	}

	private void voteMessage(VoteMsg msg, Address src)
	{ // I am the coordinator of this commit. Gathering votes
		// this context is local. access directly
		SCOReContext ctx = (SCOReContext) ctxs.get(msg.ctxID);

		if (rejectTrxs.contains(ctx.trxID))
		{ // late vote. trx already aborted
			return;
		}

		if (!msg.outcome) // voted NO
		{ // // do not wait for more votes. abort trx
			ctx.timeoutTask.cancel(); // cancel timeout
			finalizeVoteStep(ctx, false);
		}
		else
		{ // voted YES. Save proposed timestamp
			ctx.votes.add(msg.proposedTimestamp);

			if (ctx.votes.size() == ctx.expectedVotes)
			{ // last vote. Every vote was YES. send decide msg
				ctx.timeoutTask.cancel(); // cancel timeout
				finalizeVoteStep(ctx, true);
			}
		}

		System.out.println(String.format(
				"%s received a vote msg %s from %s for trx %d.", TribuDSTM
						.getLocalAddress(),
				(msg.outcome ? "(YES) -> " + msg.proposedTimestamp
						+ " [gathered " + ctx.votes.size()
						+ " votes, expecting " + ctx.expectedVotes + "]"
						: "(NO)"), src, msg.ctxID));
	}

	private void finalizeVoteStep(SCOReContext ctx, boolean outcome)
	{ // I am the coordinator of this commit.
		if (!outcome)
		{ // ensures late votes checking
			rejectTrxs.add(ctx.trxID);
		}

		int finalSid = -1;
		try
		{
			finalSid = Collections.max(ctx.votes);
		}
		catch (NoSuchElementException e)
		{ // collection is empty. ignore exception
		}
		ctx.sid = finalSid;

		DecideMsg decide = new DecideMsg(ctx.threadID, ctx.trxID, finalSid,
				outcome);
		Group group = ctx.getInvolvedNodes();
		byte[] payload = ObjectSerializer.object2ByteArray(decide);
		TribuDSTM.sendToGroup(payload, group);

		System.out
				.println(String.format("%s sending decide msg %s to group %s.",
						TribuDSTM.getLocalAddress(),
						(outcome ? ("(COMMIT) with finalSid: " + finalSid)
								: "(ABORT)"), group));
	}

	private void decideMessage(DecideMsg msg, Address src) // XXX atomically
	{ // I am a participant in this commit.
		if (msg.result)
		{
			int max = Math.max(nextId.get(), msg.finalSid);
			nextId.set(max);

			System.out.println(String.format("%s updating nextId to %d.",
					TribuDSTM.getLocalAddress(), max));

			stableQ.add(new Pair<String, Integer>(msg.trxID, msg.finalSid));
		}

		pendQ.remove(new Pair<String, Integer>(msg.trxID, -1));

		if (!msg.result)
		{
			SCOReContextState tx = (SCOReContextState) receivedTrxs
					.get(msg.trxID);
			((SCOReWriteSet) tx.ws).releaseExclusiveLocks();
			((SCOReReadSet) tx.rs).releaseSharedLocks();

			SCOReContext ctx = null;
			if (src.isLocal())
			{ // context is local. access directly
				ctx = (SCOReContext) ctxs.get(msg.ctxID);
			}
			else
			{ // context is remote. recreate from state
				ctx = (SCOReContext) ContextDelegator.getInstance();
				ctx.recreateContextFromState(tx);
			}
			ctx.processed(false);

			receivedTrxs.remove(msg.trxID);
			rejectTrxs.remove(msg.trxID);
		}

		System.out.println(String.format(
				"%s received a decide msg %s from %s for trx %d:%s.", TribuDSTM
						.getLocalAddress(),
				(msg.result ? ("(COMMIT) with finalSid: " + msg.finalSid)
						: "(ABORT)"), src, msg.ctxID, msg.trxID));
	}

	private void processTx()
	{
		if (maxSeenId.get() > commitId.get() && pendQ.isEmpty()
				&& stableQ.isEmpty())
		{
			commitId.set(maxSeenId.get());
		}

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
			// XXX atomically
			SCOReContext ctx = null;
			SCOReContextState tx = (SCOReContextState) receivedTrxs
					.get(sTx.first);

			if (tx.src.isLocal())
			{ // context is local. access directly
				ctx = (SCOReContext) ctxs.get(tx.ctxID);
			}
			else
			{ // context is remote. recreate from state
				ctx = (SCOReContext) ContextDelegator.getInstance();
				ctx.recreateContextFromState(tx);
			}
			ctx.sid = sTx.second;

			ctx.applyWriteSet();
			((SCOReWriteSet) tx.ws).releaseExclusiveLocks();
			((SCOReReadSet) tx.rs).releaseSharedLocks();

			stableQ.poll(); // remove sTx
			receivedTrxs.remove(sTx.first);

			System.out.println(TribuDSTM.getLocalAddress() + " committed trx "
					+ tx.ctxID + ":" + tx.atomicBlockId + ":" + tx.trxID
					+ " with sid: " + ctx.sid + ".");

			commitId.set(sTx.second);
			ctx.processed(true);
		}
	}

	class Pair<K, V>
	{
		public K first;
		public V second;

		public Pair(K first, V second)
		{
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean equals(Object other)
		{
			return (other instanceof Pair)
					&& (this.first.equals(((Pair<?, ?>) other).first));
		}
	}
}
