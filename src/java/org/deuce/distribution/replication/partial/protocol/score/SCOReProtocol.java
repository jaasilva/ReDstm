package org.deuce.distribution.replication.partial.protocol.score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.PartialReplicationProtocol;
import org.deuce.distribution.replication.partial.oid.PartialReplicationOID;
import org.deuce.profiling.PRProfiler;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.score.SCOReContext;
import org.deuce.transaction.score.SCOReContextState;
import org.deuce.transaction.score.SCOReReadSet;
import org.deuce.transaction.score.SCOReWriteSet;
import org.deuce.transaction.score.field.InPlaceRWLock;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transaction.score.field.Version;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

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
	// updated ONLY by bottom threads
	private final AtomicInteger commitId = new AtomicInteger(0);
	// updated by up and bottom threads
	private final AtomicInteger nextId = new AtomicInteger(0);
	// updated ONLY by bottom threads
	private final AtomicInteger maxSeenId = new AtomicInteger(0);

	private final Map<Integer, DistributedContext> ctxs = new ConcurrentHashMap<Integer, DistributedContext>();
	private final Queue<Pair<String, Integer>> pendQ = new PriorityQueue<Pair<String, Integer>>(
			50, comp); // accessed ONLY by bottom threads
	private final Queue<Pair<String, Integer>> stableQ = new PriorityQueue<Pair<String, Integer>>(
			50, comp); // accessed ONLY by bottom threads

	// accessed ONLY by bottom threads
	private final Map<String, DistributedContextState> receivedTrxs = new HashMap<String, DistributedContextState>();
	// accessed ONLY by bottom threads
	private final Set<String> rejectTrxs = new HashSet<String>();

	private static final int minReadThreads = 1;
	private final Executor pool = Executors.newFixedThreadPool(Math.max(
			Integer.getInteger("tribu.replicas") - 1, minReadThreads));

	public static final ThreadLocal<Boolean> serializationContext = new ThreadLocal<Boolean>()
	{ // false -> *NOT* read context; true -> read context
		@Override
		protected Boolean initialValue()
		{
			return false;
		}
	};

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
	}

	@Override
	public void onTxCommit(DistributedContext ctx)
	{ // I am the coordinator of this commit.
		SCOReContext sctx = (SCOReContext) ctx;
		DistributedContextState ctxState = sctx.createState();

		Group resGroup = sctx.getInvolvedNodes();
		int expVotes = resGroup.size();

		sctx.votes = new ArrayList<Integer>(expVotes);
		sctx.expectedVotes = expVotes;

		PRProfiler.onSerializationBegin(ctx.threadID);
		byte[] payload = ObjectSerializer.object2ByteArray(ctxState);
		PRProfiler.onSerializationFinish(ctx.threadID);

		PRProfiler.onPrepSend(ctx.threadID);
		PRProfiler.newMsgSent(payload.length);

		TribuDSTM.sendToGroup(payload, resGroup);

		LOGGER.debug("SEND PREP " + sctx.threadID + ":" + sctx.atomicBlockId
				+ ":" + sctx.trxID.split("-")[0]);
	}

	@Override
	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
		SCOReContext sctx = (SCOReContext) ctx;

		LOGGER.debug("FINISH " + sctx.threadID + ":" + sctx.atomicBlockId + ":"
				+ sctx.trxID.split("-")[0] + "= " + committed);
	}

	@Override
	public Object onTxRead(DistributedContext ctx, TxField field)
	{ // I am the coordinator of this read.
		PRProfiler.onTxCompleteReadBegin(ctx.threadID);
		ObjectMetadata metadata = field.getMetadata();
		SCOReContext sctx = (SCOReContext) ctx;

		boolean firstRead = !sctx.firstReadDone;
		Group group = ((PartialReplicationOID) metadata).getPartialGroup();

		if (firstRead)
		{ // first read of this transaction
			sctx.sid = commitId.get();
			sctx.firstReadDone = true;
		}

		ReadDone read;
		if (group.contains(TribuDSTM.getLocalAddress()))
		{ // local read
			PRProfiler.onTxLocalReadBegin(ctx.threadID);
			read = doRead(sctx.sid, metadata);
			PRProfiler.onTxLocalReadFinish(ctx.threadID);
		}
		else
		{ // remote read
			ReadReq req = new ReadReq(sctx.threadID, metadata, sctx.sid,
					firstRead, sctx.requestVersion);

			PRProfiler.onSerializationBegin(ctx.threadID);
			byte[] payload = ObjectSerializer.object2ByteArray(req);
			PRProfiler.onSerializationFinish(ctx.threadID);

			PRProfiler.newMsgSent(payload.length);
			PRProfiler.onTxRemoteReadBegin(ctx.threadID);

			TribuDSTM.sendToGroup(payload, group);

			try
			{ // wait for first response
				sctx.syncMsg.acquire();
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			PRProfiler.onTxRemoteReadFinish(ctx.threadID);
			read = sctx.response;
		}

		if (firstRead && read.mostRecent)
		{ // advance our snapshot id to a fresher one
			sctx.sid = Math.max(sctx.sid, read.lastCommitted);
		}

		if (sctx.isUpdate() && !read.mostRecent)
		{
			throw new TransactionException(); // abort transaction
		}
		// added to read set in onReadAccess context method

		PRProfiler.onTxCompleteReadFinish(ctx.threadID);
		return read.value;
	}

	private ReadDone doRead(int sid, ObjectMetadata metadata)
	{
		int origNextId;
		do
		{
			origNextId = nextId.get();
		}
		while (!nextId.compareAndSet(origNextId, Math.max(origNextId, sid)));

		VBoxField field = (VBoxField) TribuDSTM.getObject(metadata);

		long st = System.nanoTime();
		while (commitId.get() < sid
				&& !((InPlaceRWLock) field).isExclusiveUnlocked())
		{ // wait until (commitId.get() >= sid || ((InPlaceRWLock)
			// field).isExclusiveUnlocked()
		}
		long end = System.nanoTime();
		PRProfiler.onWaitingReadFinish(end - st);

		Version ver = field.getLastVersion().get(sid);
		boolean mostRecent = ver.equals(field.getLastVersion());
		int lastCommitted = commitId.get();

		return new ReadDone(ver.value, lastCommitted, mostRecent);
	}

	private void updateNodeTimestamps(int lastCommitted)
	{
		int origNextId;
		do
		{
			origNextId = nextId.get();
		}
		while (!nextId.compareAndSet(origNextId,
				Math.max(origNextId, lastCommitted)));

		int origMaxSeenId;
		do
		{
			origMaxSeenId = maxSeenId.get();
		}
		while (!maxSeenId.compareAndSet(origMaxSeenId,
				Math.max(origMaxSeenId, lastCommitted)));
		advanceCommitId();
	}

	@ExcludeTM
	class ReadRequestHandler implements Runnable
	{
		private ReadReq msg;
		private Address src;

		public ReadRequestHandler(ReadReq msg, Address src)
		{
			this.msg = msg;
			this.src = src;
		}

		@Override
		public void run()
		{
			readRequest(msg, src);
		}
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

		PRProfiler.onSerializationBegin(msg.ctxID);
		serializationContext.set(true);
		byte[] payload = ObjectSerializer.object2ByteArray(ret);
		serializationContext.set(false);
		PRProfiler.onSerializationFinish(msg.ctxID);

		PRProfiler.newMsgSent(payload.length);
		TribuDSTM.sendTo(payload, src);
		updateNodeTimestamps(msg.readSid);

		LOGGER.debug("READ REQ (" + src + ")");
	}

	private void readReturn(ReadRet msg, Address src)
	{ // I am the coordinator of this read.
		SCOReContext sctx = ((SCOReContext) ctxs.get(msg.ctxID));

		if (sctx.requestVersion > msg.msgVersion)
		{ // read request already responded. ignore late responses
			return;
		}
		sctx.requestVersion++;
		sctx.response = msg.read;

		updateNodeTimestamps(msg.read.lastCommitted);
		sctx.syncMsg.release();

		LOGGER.debug("READ RET (" + src + ") " + sctx.threadID + ":"
				+ sctx.atomicBlockId + ":" + sctx.trxID.split("-")[0]);
	}

	@Override
	public void onDelivery(Object obj, Address src, int size)
	{
		PRProfiler.newMsgRecv(size);

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
			pool.execute(new ReadRequestHandler((ReadReq) obj, src));
		}
		else if (obj instanceof ReadRet) // Read Return
		{ // I requested this object
			readReturn((ReadRet) obj, src);
		}

		processTx();
	}

	private void prepareMessage(SCOReContextState ctx, Address src)
	{ // I am a participant in this commit. Validate and send vote msg
		final String trxID = ctx.trxID;
		final int ctxID = ctx.ctxID;
		final int atomicBlockId = ctx.atomicBlockId;

		if (rejectTrxs.contains(trxID))
		{ // late PREPARE msg (already received DECIDE msg (NO) for this tx)
			// rejectTrxs.remove(ctx.trxID); XXX
			return;
		}
		receivedTrxs.put(trxID, ctx);

		PRProfiler.onTxValidateBegin(ctxID);
		boolean exclusiveLocks = false, sharedLocks = false, validate = false;
		final SCOReWriteSet ws = (SCOReWriteSet) ctx.ws;
		final SCOReReadSet rs = (SCOReReadSet) ctx.rs;
		boolean outcome = (exclusiveLocks = ws.getExclusiveLocks(trxID))
				&& (sharedLocks = rs.getSharedLocks(trxID))
				&& (validate = rs.validate(ctx.sid));
		PRProfiler.onTxValidateEnd(ctxID);

		int next = -1;
		if (outcome) // valid trx
		{
			next = nextId.incrementAndGet();
			pendQ.add(new Pair<String, Integer>(trxID, next));
		}
		else
		{ // vote NO! do not need the locks
			if (exclusiveLocks)
			{
				ws.releaseExclusiveLocks(trxID);
			}
			if (sharedLocks)
			{
				rs.releaseSharedLocks(trxID);
			}
		}

		VoteMsg vote = new VoteMsg(outcome, next, ctxID, trxID);

		PRProfiler.onSerializationBegin(ctxID);
		byte[] payload = ObjectSerializer.object2ByteArray(vote);
		PRProfiler.onSerializationFinish(ctxID);

		PRProfiler.newMsgSent(payload.length);
		TribuDSTM.sendTo(payload, src);

		LOGGER.debug("PREP (" + src + ") " + ctxID + ":" + atomicBlockId + ":"
				+ trxID.split("-")[0] + " " + next);
	}

	private void voteMessage(VoteMsg msg, Address src)
	{ // I am the coordinator of this commit. Gathering votes
		// this context is local. access directly
		SCOReContext ctx = (SCOReContext) ctxs.get(msg.ctxID);
		final String trxID_msg = msg.trxID;
		final String trxID = ctx.trxID;

		if (rejectTrxs.contains(trxID) || !trxID_msg.equals(trxID))
		{ // late vote. trx already aborted (no decide msg received yet)
			return;
		}

		final boolean outcome = msg.outcome;
		final List<Integer> votes = ctx.votes;
		final int proposedTimestamp = msg.proposedTimestamp;
		final int expectedVotes = ctx.expectedVotes;

		if (!outcome) // voted NO
		{ // do not wait for more votes. abort trx
			finalizeVoteStep(ctx, false);
		}
		else
		{ // voted YES. Save proposed timestamp
			votes.add(proposedTimestamp);

			if (votes.size() == expectedVotes)
			{ // last vote. Every vote was YES. send decide msg
				PRProfiler.onLastVoteReceived(ctx.threadID);

				finalizeVoteStep(ctx, true);
			}
		}

		LOGGER.debug("VOTE (" + src + ") " + ctx.threadID + ":"
				+ ctx.atomicBlockId + ":" + trxID_msg.split("-")[0] + " "
				+ msg.proposedTimestamp);
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

		PRProfiler.onSerializationBegin(ctx.threadID);
		byte[] payload = ObjectSerializer.object2ByteArray(decide);
		PRProfiler.onSerializationFinish(ctx.threadID);

		PRProfiler.newMsgSent(payload.length);
		TribuDSTM.sendToGroup(payload, group);

		LOGGER.debug("SEND DEC " + ctx.threadID + ":" + ctx.atomicBlockId + ":"
				+ ctx.trxID.split("-")[0] + " " + finalSid);
	}

	private synchronized void decideMessage(DecideMsg msg, Address src)
	{ // I am a participant in this commit. *atomically*
		final String trxID = msg.trxID;
		final boolean result = msg.result;
		final int finalSid = msg.finalSid;

		if (result)
		{ // DECIDE YES
			int origNextId;
			do
			{
				origNextId = nextId.get();
			}
			while (!nextId.compareAndSet(origNextId,
					Math.max(origNextId, finalSid)));
			stableQ.add(new Pair<String, Integer>(trxID, finalSid));
		}

		boolean remove = pendQ.remove(new Pair<String, Integer>(trxID, -1));
		advanceCommitId();

		SCOReContextState tx = (SCOReContextState) receivedTrxs.get(trxID);
		final int ctxID = msg.ctxID;
		if (!result)
		{ // DECIDE NO
			if (tx != null)
			{ // received DECIDE msg *after* PREPARE msg (someone voted NO)
				if (remove) // I only have the locks if I voted YES
				{ // and put the tx in the pendQ
					((SCOReReadSet) tx.rs).releaseSharedLocks(trxID);
					((SCOReWriteSet) tx.ws).releaseExclusiveLocks(trxID);
				}

				SCOReContext ctx = null;
				if (src.isLocal())
				{ // context is local. access directly
					ctx = (SCOReContext) ctxs.get(ctxID);
					ctx.processed(false);
				}

				receivedTrxs.remove(trxID);
			}
			else
			{ // received DECIDE msg *before* PREPARE msg (someone voted NO)
				if (src.isLocal())
				{ // context is local. access directly
					SCOReContext ctx = (SCOReContext) ctxs.get(ctxID);
					ctx.processed(false);
				}

				rejectTrxs.add(trxID);
			}
		}

		LOGGER.debug("DEC (" + src + ") " + ctxID + ":" + tx.atomicBlockId
				+ ":" + trxID.split("-")[0] + " " + msg.finalSid);
	}

	private synchronized void processTx()
	{
		advanceCommitId();

		// snId's value determines the next snapshot that can safely be made
		// public
		int snId = -1;
		while (true)
		{
			if (stableQ.isEmpty())
			{ // nothing to do
				if (snId != -1)
				{ // it is safe to make snapshot visible
					commitId.set(snId);
				}
				advanceCommitId();
				return;
			}

			Pair<String, Integer> sTx = stableQ.peek();
			Pair<String, Integer> pTx = pendQ.peek();

			if (pTx != null && pTx.second <= sTx.second)
			{ // there are still some trxs that can be serialized before/with
				// sTx
				if (snId != -1)
				{ // it is safe to make snapshot visible
					commitId.set(snId);
				}
				return;
			}

			// *atomically*
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
			if (ctx.sid > snId)
			{ // this snapshot is fresher than snId, it is safe to make
				commitId.set(snId); // previous snapshot visible
			}
			snId = ctx.sid;

			ctx.applyWriteSet();

			((SCOReReadSet) tx.rs).releaseSharedLocks(sTx.first);
			((SCOReWriteSet) tx.ws).releaseExclusiveLocks(sTx.first);

			stableQ.poll(); // remove sTx
			receivedTrxs.remove(sTx.first);

			LOGGER.debug("COMMIT " + ctx.threadID + ":" + ctx.atomicBlockId
					+ ":" + ctx.trxID.split("-")[0] + " " + ctx.sid);

			ctx.processed(true);
		}
	}

	final private void advanceCommitId()
	{
		int origCommitId;
		do
		{
			origCommitId = commitId.get();
			if (origCommitId >= maxSeenId.get() || !pendQ.isEmpty()
					|| !stableQ.isEmpty())
			{
				return;
			}
		}
		while (!commitId.compareAndSet(origCommitId, maxSeenId.get()));
	}

	@ExcludeTM
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

		public String toString()
		{
			return "(" + first + "," + second + ")";
		}
	}
}
