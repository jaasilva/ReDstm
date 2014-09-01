package org.deuce.distribution.replication.partial.protocol.score;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;

import org.apache.log4j.Logger;
import org.deuce.distribution.Defaults;
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
import org.deuce.transaction.score.Context;
import org.deuce.transaction.score.ContextState;
import org.deuce.transaction.score.field.InPlaceRWLock;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transaction.score.field.Version;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 */
@ExcludeTM
public class SCORe extends PartialReplicationProtocol implements
		DeliverySubscriber
{
	private static final Logger LOGGER = Logger.getLogger(SCORe.class);

	protected final Map<Integer, DistributedContext> ctxs = new ConcurrentHashMap<Integer, DistributedContext>();

	// accessed ONLY by bottom threads
	private final Queue<Pair> pendQ = new PriorityQueue<Pair>(1000);
	// accessed ONLY by bottom threads
	private final Queue<Pair> stableQ = new PriorityQueue<Pair>(1000);

	// accessed ONLY by bottom threads
	private final Map<String, DistributedContextState> receivedTxns = new HashMap<String, DistributedContextState>(
			1000);
	// accessed ONLY by bottom threads
	private final Set<String> rejectTxns = new HashSet<String>();

	private static final int minReadThreads = 1;
	private final Executor pool = Executors.newFixedThreadPool(Math.max(
			Integer.getInteger(Defaults._REPLICAS) - 1, minReadThreads));

	private static final List<DistributedContextState> toBeProcessed = new ArrayList<DistributedContextState>();

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
		Context sctx = (Context) ctx;
		DistributedContextState ctxState = sctx.createState();

		Group resGroup = sctx.getInvolvedNodes();
		int expVotes = resGroup.size();
		Profiler.whatNodes(expVotes);

		sctx.maxVote = 0;
		sctx.receivedVotes = 0;
		sctx.expectedVotes = expVotes;

		byte[] payload = ObjectSerializer.object2ByteArray(ctxState);

		Profiler.newMsgSent(payload.length);
		Profiler.onPrepSend(ctx.threadID);
		TribuDSTM.sendReliably(payload, resGroup);

		LOGGER.debug("SEND PREP " + sctx.threadID + ":" + sctx.atomicBlockId
				+ ":" + sctx.txnID.split("-")[0]);
	}

	@Override
	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
		Context sctx = (Context) ctx;
		LOGGER.debug("FINISH " + sctx.threadID + ":" + sctx.atomicBlockId + ":"
				+ sctx.txnID.split("-")[0] + "= " + committed);
	}

	@Override
	public Object onTxRead(DistributedContext ctx, TxField field)
	{ // I am the coordinator of this read.
		return processRead((Context) ctx, field);
	}

	protected ReadDone processRead(Context sctx, TxField field)
	{
		ObjectMetadata meta = field.getMetadata();
		boolean firstRead = !sctx.firstReadDone;
		Group p_group = ((PartialReplicationOID) meta).getPartialGroup();
		Group group = ((PartialReplicationOID) meta).getGroup();
		ReadDone read = null;

		// If I belong to the local group, then I replicate this object
		boolean localObj = p_group.isLocal();
		/*
		 * If the groups are equal I have the graph from the partial txField
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

	protected ReadDone remoteRead(Context sctx, ObjectMetadata metadata,
			boolean firstRead, Group p_group)
	{
		ReadReq req = new ReadReq(sctx.threadID, metadata, sctx.sid, firstRead,
				sctx.requestVersion);

		byte[] payload = ObjectSerializer.object2ByteArray(req);

		Profiler.newMsgSent(payload.length);
		TribuDSTM.sendReliably(payload, p_group);

		LOGGER.debug("SEND READ REQ " + sctx.txnID.split("-")[0] + " "
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

	protected void updateNodeTimestamps(int lastCommitted)
	{
		synchronized (Context.nextId)
		{
			Context.nextId.set(Math.max(Context.nextId.get(), lastCommitted));
		}

		synchronized (Context.maxSeenId)
		{
			Context.maxSeenId.set(Math.max(Context.maxSeenId.get(),
					lastCommitted));
		}

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

		if (msg.firstRead && (commit = Context.commitId.get()) > newReadSid)
		{
			newReadSid = commit;
		}

		ReadDone read = doReadRemote(newReadSid, msg.metadata);
		ReadRet ret = new ReadRet(msg.ctxID, msg.msgVersion, read);

		isRead.set(true); // enter read context
		byte[] payload = ObjectSerializer.object2ByteArray(ret);
		isRead.set(false); // exit read context

		Profiler.newMsgSent(payload.length);
		isRead.set(true); // enter read context
		TribuDSTM.sendReliably(payload, src);
		isRead.set(false); // exit read context
		updateNodeTimestamps(msg.readSid);

		LOGGER.debug("READ REQ (" + src + ") "
				+ msg.metadata.toString().split("-")[0]);
	}

	protected ReadDone doReadRemote(int sid, ObjectMetadata metadata)
	{
		synchronized (Context.nextId)
		{
			Context.nextId.set(Math.max(Context.nextId.get(), sid));
		}

		VBoxField field = (VBoxField) TribuDSTM.getObject(metadata);

		long st = System.nanoTime();
		while (Context.commitId.get() < sid
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
		int lastCommitted = Context.commitId.get();
		return new ReadDone(ver.value, lastCommitted, mostRecent);
	}

	protected void readReturn(ReadRet msg, Address src)
	{ // I am the coordinator of this read.
		Context sctx = ((Context) ctxs.get(msg.ctxID));

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
				+ sctx.atomicBlockId + ":" + sctx.txnID.split("-")[0] + " "
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
			prepareMessage((ContextState) obj, src);
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

	private void prepareMessage(ContextState ctx, Address src)
	{ // I am a participant in this commit. Validate and send vote msg
		final String txnID = ctx.txnID;
		final int ctxID = ctx.ctxID;
		final int atomicBlockId = ctx.atomicBlockId;

		if (rejectTxns.contains(txnID))
		{ // late PREPARE msg (already received DECIDE msg (NO) for this tx)
			// rejectTxns.remove(ctx.txnID); CHECKME why?
			return;
		}
		receivedTxns.put(txnID, ctx);

		Context sctx = null;
		if (src.isLocal())
		{ // context is local. access directly
			sctx = (Context) ctxs.get(ctxID);
		}
		else
		{ // context is remote. recreate from state
			sctx = (Context) ContextDelegator.getInstance();
			sctx.recreateContextFromState(ctx);
		}

		boolean outcome = sctx.validate();

		int next = -1;
		if (outcome) // valid txn
		{
			synchronized (Context.nextId)
			{
				next = Context.nextId.incrementAndGet();
			}
			pendQ.add(new Pair(txnID, next));
		}

		VoteMsg vote = new VoteMsg(outcome, next, ctxID, txnID);

		byte[] payload = ObjectSerializer.object2ByteArray(vote);

		Profiler.newMsgSent(payload.length);
		TribuDSTM.sendReliably(payload, src);

		LOGGER.debug("PREP (" + src + ") " + ctxID + ":" + atomicBlockId + ":"
				+ txnID.split("-")[0] + " " + next);
	}

	private void voteMessage(VoteMsg msg, Address src)
	{ // I am the coordinator of this commit. Gathering votes
		// this context is local. access directly
		Context ctx = (Context) ctxs.get(msg.ctxID);
		final String txnID_msg = msg.txnID;
		final String txnID = ctx.txnID;

		if (rejectTxns.contains(txnID) || !txnID_msg.equals(txnID))
		{ // late vote. txn already/to be aborted
			return;
		}

		final boolean outcome = msg.outcome;
		final int proposedTimestamp = msg.proposedTimestamp;
		final int expectedVotes = ctx.expectedVotes;

		LOGGER.debug("VOTE (" + src + ") " + ctx.threadID + ":"
				+ ctx.atomicBlockId + ":" + txnID_msg.split("-")[0] + " "
				+ msg.proposedTimestamp);

		if (!outcome) // voted NO
		{ // do not wait for more votes. abort txn
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

	private void finalizeVoteStep(Context ctx, boolean outcome)
	{ // I am the coordinator of this commit.
		if (!outcome)
		{ // ensures late vote checking
			rejectTxns.add(ctx.txnID);
		}

		int finalSid = ctx.maxVote;
		ctx.sid = finalSid;

		DecideMsg decide = new DecideMsg(ctx.threadID, ctx.txnID, finalSid,
				outcome);
		Group group = ctx.getInvolvedNodes();

		byte[] payload = ObjectSerializer.object2ByteArray(decide);

		Profiler.newMsgSent(payload.length);
		TribuDSTM.sendReliably(payload, group);

		LOGGER.debug("SEND DEC " + ctx.threadID + ":" + ctx.atomicBlockId + ":"
				+ ctx.txnID.split("-")[0] + " " + finalSid);
	}

	private synchronized void decideMessage(DecideMsg msg, Address src)
	{ // I am a participant in this commit. *atomically*
		final String txnID = msg.txnID;
		final boolean result = msg.result;
		final int finalSid = msg.finalSid;
		final int ctxID = msg.ctxID;

		LOGGER.debug("DEC (" + src + ") " + ctxID + ":_:" + txnID.split("-")[0]
				+ " " + msg.finalSid);

		if (result)
		{ // DECIDE YES
			synchronized (Context.nextId)
			{
				Context.nextId.set(Math.max(Context.nextId.get(), finalSid));
			}

			stableQ.add(new Pair(txnID, finalSid));
		}

		boolean remove = pendQ.remove(new Pair(txnID, -1));
		advanceCommitId();

		ContextState tx = (ContextState) receivedTxns.get(txnID);
		if (!result)
		{ // DECIDE NO (someone voted NO)
			if (tx != null)
			{ // received DECIDE msg *after* PREPARE msg
				if (remove) // I only have the locks if I voted YES
				{ // and put the tx in the pendQ
					Context sctx = (Context) ContextDelegator.getInstance();
					sctx.recreateContextFromState(tx);
					sctx.unlock(); // release shared and exclusive locks
				}

				receivedTxns.remove(txnID);
			}
			else
			{ // received DECIDE msg *before* PREPARE msg
				rejectTxns.add(txnID);
			}

			if (src.isLocal())
			{ // context is local. access directly
				Context ctx = (Context) ctxs.get(ctxID);
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
					synchronized (Context.commitId)
					{
						Context.commitId.set(snId);
					}
					releaseTxns();
				}
				advanceCommitId();
				return;
			}

			Pair sTxn = stableQ.peek();
			Pair pTxn = pendQ.peek();

			if (pTxn != null && pTxn.sid <= sTxn.sid)
			{ // there are still some txns that can be serialized before/with
				// sTxn
				if (snId != -1)
				{ // it is safe to make snapshot visible
					synchronized (Context.commitId)
					{
						Context.commitId.set(snId);
					}
					releaseTxns();
				}
				return;
			}

			// *atomically*
			Context ctx = null;
			ContextState txn = (ContextState) receivedTxns.get(sTxn.txnId);

			if (txn.src.isLocal())
			{ // context is local. access directly
				ctx = (Context) ctxs.get(txn.ctxID);
			}
			else
			{ // context is remote. recreate from state
				ctx = (Context) ContextDelegator.getInstance();
				ctx.recreateContextFromState(txn);
			}

			ctx.sid = sTxn.sid;
			if (snId != -1 && ctx.sid > snId)
			{ // this snapshot is fresher than snId, it is safe to make
				// the previous snapshot visible
				synchronized (Context.commitId)
				{
					Context.commitId.set(snId);
				}
				releaseTxns();
			}
			snId = ctx.sid;

			ctx.applyWriteSet();

			ctx.unlock(); // release shared and exclusive locks
			stableQ.poll(); // remove sTxn
			toBeProcessed.add(receivedTxns.remove(sTxn.txnId));

			LOGGER.debug("COMMIT " + ctx.threadID + ":" + ctx.atomicBlockId
					+ ":" + ctx.txnID.split("-")[0] + " " + ctx.sid);
		}
	}

	private void releaseTxns()
	{
		Iterator<DistributedContextState> it = toBeProcessed.iterator();

		while (it.hasNext())
		{
			ContextState ctx = (ContextState) it.next();
			if (ctx.src.isLocal())
			{ // context is local. access directly
				Context sctx = (Context) ctxs.get(ctx.ctxID);
				sctx.processed(true);
			}
			it.remove();
		}
	}

	private void advanceCommitId()
	{ /* atomically do */
		synchronized (Context.commitId)
		{
			if (Context.maxSeenId.get() > Context.commitId.get()
					&& pendQ.isEmpty() && stableQ.isEmpty())
			{
				Context.commitId.set(Context.maxSeenId.get());
			}
		}
	}

	@ExcludeTM
	class Pair implements Comparable<Pair>
	{
		public String txnId;
		public int sid;

		public Pair(String first, int second)
		{
			this.txnId = first;
			this.sid = second;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Pair other = (Pair) obj;
			if (txnId == null)
			{
				if (other.txnId != null)
					return false;
			}
			else if (!txnId.equals(other.txnId))
				return false;
			return true;
		}

		@Override
		public String toString()
		{
			return "(" + txnId + "," + sid + ")";
		}

		@Override
		public int compareTo(Pair other)
		{
			if (other == null)
				return -1;
			return this.sid - other.sid;
		}
	}
}
