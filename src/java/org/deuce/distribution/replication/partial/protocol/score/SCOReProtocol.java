package org.deuce.distribution.replication.partial.protocol.score;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;
import org.deuce.Defaults;
import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.msgs.ControlMessage;
import org.deuce.distribution.replication.partial.PartialReplicationOID;
import org.deuce.distribution.replication.partial.PartialReplicationProtocol;
import org.deuce.distribution.replication.partial.protocol.score.msgs.DecideMsg;
import org.deuce.distribution.replication.partial.protocol.score.msgs.ReadDone;
import org.deuce.distribution.replication.partial.protocol.score.msgs.ReadReq;
import org.deuce.distribution.replication.partial.protocol.score.msgs.ReadRet;
import org.deuce.distribution.replication.partial.protocol.score.msgs.VoteMsg;
import org.deuce.profiling.Profiler;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.score.SCOReContext;
import org.deuce.transaction.score.SCOReContextState;
import org.deuce.transaction.score.field.InPlaceRWLock;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transaction.score.field.Version;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 */
@ExcludeTM
public class SCOReProtocol extends PartialReplicationProtocol implements
		DeliverySubscriber
{
	private static final Logger LOGGER = Logger.getLogger(SCOReProtocol.class);
	private final Comparator<Pair<String, Integer>> comp = new Comparator<Pair<String, Integer>>()
	{
		public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2)
		{
			return o1.second - o2.second;
		}
	};

	protected final Map<Integer, DistributedContext> ctxs = new ConcurrentHashMap<Integer, DistributedContext>();
	private final Queue<Pair<String, Integer>> pendQ = new PriorityBlockingQueue<Pair<String, Integer>>(
			1000, comp); // accessed ONLY by bottom threads
	private final Queue<Pair<String, Integer>> stableQ = new PriorityBlockingQueue<Pair<String, Integer>>(
			1000, comp); // accessed ONLY by bottom threads

	// accessed ONLY by bottom threads
	private final Map<String, DistributedContextState> receivedTrxs = new ConcurrentHashMap<String, DistributedContextState>(
			1000);
	// accessed ONLY by bottom threads
	private final Set<String> rejectTrxs = Collections
			.newSetFromMap(new ConcurrentHashMap<String, Boolean>(1000));

	private static final int minReadThreads = 1;
	private final Executor pool = Executors.newFixedThreadPool(Math.max(
			Integer.getInteger(Defaults._REPLICAS) - 1, minReadThreads));

	private static final Set<DistributedContextState> toBeProcessed = Collections
			.newSetFromMap(new ConcurrentHashMap<DistributedContextState, Boolean>());

	@Override
	public void init()
	{
		TribuDSTM.subscribeDeliveries(this);
	}

	@Override
	public void onTxContextCreation(DistributedContext ctx)
	{
		ctxs.put(ctx.threadID, ctx);
		LOGGER.trace("Created DistributedContext: " + ctx.threadID);
	}

	@Override
	public void onTxBegin(DistributedContext ctx)
	{
		SCOReContext sctx = (SCOReContext) ctx;
		LOGGER.debug("BEGIN " + sctx.threadID + ":" + sctx.atomicBlockId + ":"
				+ sctx.trxID.split("-")[0]);
	}

	@Override
	public void onTxCommit(DistributedContext ctx)
	{ // I am the coordinator of this commit.
		Profiler.onTxDistCommitBegin(ctx.threadID);
		SCOReContext sctx = (SCOReContext) ctx;
		DistributedContextState ctxState = sctx.createState();

		Group resGroup = sctx.getInvolvedNodes();
		int expVotes = resGroup.size();
		Profiler.whatNodes(expVotes);

		sctx.maxVote = 0;
		sctx.receivedVotes = 0;
		sctx.expectedVotes = expVotes;

		Profiler.onSerializationBegin(ctx.threadID);
		byte[] payload = ObjectSerializer.object2ByteArray(ctxState);
		Profiler.onSerializationFinish(ctx.threadID);

		Profiler.newMsgSent(payload.length);
		Profiler.onPrepSend(ctx.threadID);

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
		return processRead((SCOReContext) ctx, field);
	}

	protected ReadDone processRead(SCOReContext sctx, TxField field)
	{
		ObjectMetadata meta = field.getMetadata();
		boolean firstRead = !sctx.firstReadDone;
		Group p_group = ((PartialReplicationOID) meta).getPartialGroup();
		Group group = ((PartialReplicationOID) meta).getGroup();
		ReadDone read = null;

		// If I belong to the local group, then I replicate this object
		boolean localObj = p_group.isLocal();
		/*
		 * if the groups are equal I have the graph from the partial txField
		 * down cached in the locator table
		 */
		boolean localGraph = p_group.equals(group);

		if (localObj || localGraph)
		{ // Do *LOCAL* read
			Profiler.onTxLocalReadBegin(sctx.threadID);
			read = sctx.doReadLocal((VBoxField) field);
			Profiler.onTxLocalReadFinish(sctx.threadID);
		}
		else
		{ // Do *REMOTE* read
			Profiler.onTxRemoteReadBegin(sctx.threadID);
			read = remoteRead(sctx, meta, firstRead, p_group);
			Profiler.onTxRemoteReadFinish(sctx.threadID);
		}

		return read;
	}

	protected ReadDone remoteRead(SCOReContext sctx, ObjectMetadata metadata,
			boolean firstRead, Group p_group)
	{
		ReadReq req = new ReadReq(sctx.threadID, metadata, sctx.sid, firstRead,
				sctx.requestVersion);

		Profiler.onSerializationBegin(sctx.threadID);
		byte[] payload = ObjectSerializer.object2ByteArray(req);
		Profiler.onSerializationFinish(sctx.threadID);

		Profiler.newMsgSent(payload.length);
		TribuDSTM.sendToGroup(payload, p_group);

		LOGGER.debug("SEND READ REQ " + sctx.trxID.split("-")[0] + " "
				+ sctx.requestVersion);

		try
		{ // wait for first response
			sctx.syncMsg.acquire();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		return sctx.response;
	}

	protected ReadDone doReadRemote(int sid, ObjectMetadata metadata)
	{
		int origNextId;
		do
		{
			origNextId = SCOReContext.nextId.get();
		} while (!SCOReContext.nextId.compareAndSet(origNextId,
				Math.max(origNextId, sid)));

		VBoxField field = (VBoxField) TribuDSTM.getObject(metadata);

		long st = System.nanoTime();
		while (SCOReContext.commitId.get() < sid
				&& !((InPlaceRWLock) field).isExclusiveUnlocked())
		{ /*
		 * wait until (commitId.get() >= sid || ((InPlaceRWLock)
		 * field).isExclusiveUnlocked())
		 */
		}
		long end = System.nanoTime();
		Profiler.onWaitingRead(end - st);

		Version ver = field.getLastVersion().get(sid);
		boolean mostRecent = ver.equals(field.getLastVersion());
		int lastCommitted = SCOReContext.commitId.get();
		return new ReadDone(ver.value, lastCommitted, mostRecent);
	}

	protected void updateNodeTimestamps(int lastCommitted)
	{
		int origNextId;
		do
		{
			origNextId = SCOReContext.nextId.get();
		} while (!SCOReContext.nextId.compareAndSet(origNextId,
				Math.max(origNextId, lastCommitted)));

		int origMaxSeenId;
		do
		{
			origMaxSeenId = SCOReContext.maxSeenId.get();
		} while (!SCOReContext.maxSeenId.compareAndSet(origMaxSeenId,
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
		int commit;

		if (msg.firstRead
				&& (commit = SCOReContext.commitId.get()) > newReadSid)
		{
			newReadSid = commit;
		}

		ReadDone read = doReadRemote(newReadSid, msg.metadata);
		ReadRet ret = new ReadRet(msg.ctxID, msg.msgVersion, read);

		Profiler.onSerializationBegin(msg.ctxID);
		serializationReadContext.set(true); // enter read context
		byte[] payload = ObjectSerializer.object2ByteArray(ret);
		serializationReadContext.set(false); // exit read context
		Profiler.onSerializationFinish(msg.ctxID);

		Profiler.newMsgSent(payload.length);
		serializationReadContext.set(true); // enter read context
		TribuDSTM.sendTo(payload, src);
		serializationReadContext.set(false); // exit read context
		updateNodeTimestamps(msg.readSid);

		LOGGER.debug("READ REQ (" + src + ") "
				+ msg.metadata.toString().split("-")[0]);
	}

	protected void readReturn(ReadRet msg, Address src)
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

		Profiler.remoteReadOk();

		LOGGER.debug("READ RET (" + src + ") " + sctx.threadID + ":"
				+ sctx.atomicBlockId + ":" + sctx.trxID.split("-")[0] + " "
				+ msg.msgVersion);
	}

	@Override
	public void onDelivery(Object obj, Address src, int size)
	{
		Profiler.newMsgRecv(size);
		if (obj instanceof ControlMessage)
		{
			processControlMessage((ControlMessage) obj);
		}
		else if (obj instanceof DistributedContextState) // Prepare Message
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
			Profiler.newRemoteReadRecv(size);
			readReturn((ReadRet) obj, src);
		}
		processTx();
	}

	protected void processControlMessage(ControlMessage obj)
	{
	}

	private void prepareMessage(SCOReContextState ctx, Address src)
	{ // I am a participant in this commit. Validate and send vote msg
		final String trxID = ctx.trxID;
		final int ctxID = ctx.ctxID;
		final int atomicBlockId = ctx.atomicBlockId;

		if (rejectTrxs.contains(trxID))
		{ // late PREPARE msg (already received DECIDE msg (NO) for this tx)
			// rejectTrxs.remove(ctx.trxID); XXX why?
			return;
		}
		receivedTrxs.put(trxID, ctx);

		SCOReContext sctx = null;
		if (src.isLocal())
		{ // context is local. access directly
			sctx = (SCOReContext) ctxs.get(ctxID);
		}
		else
		{ // context is remote. recreate from state
			sctx = (SCOReContext) ContextDelegator.getInstance();
			sctx.recreateContextFromState(ctx);
		}

		boolean outcome = sctx.validate();

		int next = -1;
		if (outcome) // valid trx
		{
			next = SCOReContext.nextId.incrementAndGet();
			pendQ.add(new Pair<String, Integer>(trxID, next));
		}

		VoteMsg vote = new VoteMsg(outcome, next, ctxID, trxID);

		Profiler.onSerializationBegin(ctxID);
		byte[] payload = ObjectSerializer.object2ByteArray(vote);
		Profiler.onSerializationFinish(ctxID);

		Profiler.newMsgSent(payload.length);
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
		{ // late vote. trx already/to be aborted
			return;
		}

		final boolean outcome = msg.outcome;
		final int proposedTimestamp = msg.proposedTimestamp;
		final int expectedVotes = ctx.expectedVotes;

		LOGGER.debug("VOTE (" + src + ") " + ctx.threadID + ":"
				+ ctx.atomicBlockId + ":" + trxID_msg.split("-")[0] + " "
				+ msg.proposedTimestamp);

		if (!outcome) // voted NO
		{ // do not wait for more votes. abort trx
			finalizeVoteStep(ctx, false);
		}
		else
		{ // voted YES. Save proposed timestamp
			ctx.receivedVotes++;
			if (proposedTimestamp > ctx.maxVote)
			{
				ctx.maxVote = proposedTimestamp;
			}
			if (ctx.receivedVotes == expectedVotes)
			{ // last vote. Every vote was YES. send decide msg
				Profiler.onLastVoteReceived(ctx.threadID);
				finalizeVoteStep(ctx, true);
			}
		}
	}

	private void finalizeVoteStep(SCOReContext ctx, boolean outcome)
	{ // I am the coordinator of this commit.
		if (!outcome)
		{ // ensures late vote checking
			rejectTrxs.add(ctx.trxID);
		}

		int finalSid = ctx.maxVote;
		ctx.sid = finalSid;

		DecideMsg decide = new DecideMsg(ctx.threadID, ctx.trxID, finalSid,
				outcome);
		Group group = ctx.getInvolvedNodes();

		Profiler.onSerializationBegin(ctx.threadID);
		byte[] payload = ObjectSerializer.object2ByteArray(decide);
		Profiler.onSerializationFinish(ctx.threadID);

		Profiler.newMsgSent(payload.length);
		TribuDSTM.sendToGroup(payload, group);

		LOGGER.debug("SEND DEC " + ctx.threadID + ":" + ctx.atomicBlockId + ":"
				+ ctx.trxID.split("-")[0] + " " + finalSid);
	}

	private synchronized void decideMessage(DecideMsg msg, Address src)
	{ // I am a participant in this commit. *atomically*
		final String trxID = msg.trxID;
		final boolean result = msg.result;
		final int finalSid = msg.finalSid;
		final int ctxID = msg.ctxID;

		LOGGER.debug("DEC (" + src + ") " + ctxID + ":_:" + trxID.split("-")[0]
				+ " " + msg.finalSid);

		if (result)
		{ // DECIDE YES
			int origNextId;
			do
			{
				origNextId = SCOReContext.nextId.get();
			} while (!SCOReContext.nextId.compareAndSet(origNextId,
					Math.max(origNextId, finalSid)));
			stableQ.add(new Pair<String, Integer>(trxID, finalSid));
		}

		boolean remove = pendQ.remove(new Pair<String, Integer>(trxID, -1));
		advanceCommitId();

		SCOReContextState tx = (SCOReContextState) receivedTrxs.get(trxID);
		if (!result)
		{ // DECIDE NO (someone voted NO)
			if (tx != null)
			{ // received DECIDE msg *after* PREPARE msg
				if (remove) // I only have the locks if I voted YES
				{ // and put the tx in the pendQ
					SCOReContext sctx = (SCOReContext) ContextDelegator
							.getInstance();
					sctx.recreateContextFromState(tx);
					sctx.unlock(); // release shared and exclusive locks
				}

				receivedTrxs.remove(trxID);
			}
			else
			{ // received DECIDE msg *before* PREPARE msg
				rejectTrxs.add(trxID);
			}

			if (src.isLocal())
			{ // context is local. access directly
				SCOReContext ctx = (SCOReContext) ctxs.get(ctxID);
				Profiler.onTxDistCommitFinish(ctxID);
				ctx.processed(false);
			}
		}
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
					SCOReContext.commitId.set(snId);
					releaseTrxs();
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
					SCOReContext.commitId.set(snId);
					releaseTrxs();
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
			if (snId != -1 && ctx.sid > snId)
			{ // this snapshot is fresher than snId, it is safe to make
				SCOReContext.commitId.set(snId); // the previous snapshot
													// visible
				releaseTrxs();
			}
			snId = ctx.sid;

			ctx.applyWriteSet();

			ctx.unlock(); // release shared and exclusive locks
			stableQ.poll(); // remove sTx
			toBeProcessed.add(receivedTrxs.remove(sTx.first));

			LOGGER.debug("COMMIT " + ctx.threadID + ":" + ctx.atomicBlockId
					+ ":" + ctx.trxID.split("-")[0] + " " + ctx.sid);
		}
	}

	private void releaseTrxs()
	{
		Iterator<DistributedContextState> it = toBeProcessed.iterator();

		while (it.hasNext())
		{
			SCOReContextState ctx = (SCOReContextState) it.next();
			SCOReContext sctx = null;
			if (ctx.src.isLocal())
			{ // context is local. access directly
				sctx = (SCOReContext) ctxs.get(ctx.ctxID);
				Profiler.onTxDistCommitFinish(sctx.threadID);
				sctx.processed(true);
			}
			it.remove();
		}
	}

	private void advanceCommitId()
	{ /* atomically do */
		int origCommitId;
		do
		{
			origCommitId = SCOReContext.commitId.get();
			if (origCommitId >= SCOReContext.maxSeenId.get()
					|| !pendQ.isEmpty() || !stableQ.isEmpty())
			{
				return;
			}
		} while (!SCOReContext.commitId.compareAndSet(origCommitId,
				SCOReContext.maxSeenId.get()));
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

		@Override
		public String toString()
		{
			return "(" + first + "," + second + ")";
		}
	}
}
