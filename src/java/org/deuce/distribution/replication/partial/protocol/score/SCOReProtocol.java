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

	private final AtomicInteger commitId = new AtomicInteger(0); // updated ONLY by bottom threads
	private final AtomicInteger nextId = new AtomicInteger(0); // updated by up and bottom threads
	private final AtomicInteger maxSeenId = new AtomicInteger(0); // updated ONLY by bottom threads

	private final Map<Integer, DistributedContext> ctxs = new ConcurrentHashMap<Integer, DistributedContext>();

	private final Queue<Pair<String, Integer>> pendQ = new PriorityQueue<Pair<String, Integer>>(
			50, comp); // accessed only by bottom threads
	private final Queue<Pair<String, Integer>> stableQ = new PriorityQueue<Pair<String, Integer>>(
			50, comp); // accessed only by bottom threads

	private final Map<String, DistributedContextState> receivedTrxs = new HashMap<String, DistributedContextState>(); // accessed only by bottom threads
	private final Set<String> rejectTrxs = new HashSet<String>(); // accessed only by bottom threads

	private static final int minReadThreads = 2;
	private final Executor pool = Executors.newFixedThreadPool(Math.max(
			TribuDSTM.getNumGroups() / 2, minReadThreads));

	public static final ThreadLocal<Boolean> serializationContext = new ThreadLocal<Boolean>()
	{ // false -> *not* read context; true -> read context
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

		SCOReContext sctx = (SCOReContext) ctx;
		LOGGER.debug("* onTxContextCreation " + sctx.threadID + ":"
				+ sctx.atomicBlockId + ":" + sctx.trxID);
	}

	@Override
	public void onTxBegin(DistributedContext ctx)
	{
		SCOReContext sctx = (SCOReContext) ctx;
		LOGGER.debug("* onTxBegin " + sctx.threadID + ":" + sctx.atomicBlockId
				+ ":" + sctx.trxID);
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

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		log.append("onTxCommit " + sctx.threadID + ":" + sctx.atomicBlockId
				+ ":" + sctx.trxID + "\n");
		log.append("Coordinator= " + ((SCOReContextState) ctxState).src
				+ " sid= " + sctx.sid + "\n");
		log.append("Send PREP msg to= " + resGroup + "\n");
		log.append("WS: " + sctx.writeSet + "\n");
		log.append("RS: " + sctx.readSet + "\n");
		log.append("------------------------------------------");
		LOGGER.debug(log.toString());

		PRProfiler.onSerializationBegin(ctx.threadID);
		byte[] payload = ObjectSerializer.object2ByteArray(ctxState);
		PRProfiler.onSerializationFinish(ctx.threadID);

		PRProfiler.onPrepSend(ctx.threadID);
		PRProfiler.newMsgSent(payload.length);

		TribuDSTM.sendToGroup(payload, resGroup);
	}

	@Override
	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
		SCOReContext sctx = (SCOReContext) ctx;

		LOGGER.warn("* onTxFinished " + sctx.threadID + ":"
				+ sctx.atomicBlockId + ":" + sctx.trxID + "= " + committed);
	}

	@Override
	public Object onTxRead(DistributedContext ctx, ObjectMetadata metadata)
	{ // I am the coordinator of this read.
		PRProfiler.onTxCompleteReadBegin(ctx.threadID);

		SCOReContext sctx = (SCOReContext) ctx;

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		log.append("onTxRead " + sctx.threadID + ":" + sctx.atomicBlockId + ":"
				+ sctx.trxID + "\n");

		boolean firstRead = !sctx.firstReadDone;
		Group group = ((PartialReplicationOID) metadata).getGroup();

		if (firstRead)
		{ // first read of this transaction
			sctx.sid = commitId.get();
			sctx.firstReadDone = true;
		}

		log.append("Metadata= " + metadata + "\n");
		log.append("firstRead= " + firstRead + "\n");

		ReadDone read;
		if (group.contains(TribuDSTM.getLocalAddress()))
		{ // local read
			PRProfiler.onTxLocalReadBegin(ctx.threadID);
			read = doRead(sctx.sid, metadata);
			PRProfiler.onTxLocalReadFinish(ctx.threadID);

			log.append("Local read (sid=" + sctx.sid + ")\n");
		}
		else
		{ // remote read
			ReadReq req = new ReadReq(sctx.threadID, metadata, sctx.sid,
					firstRead, sctx.requestVersion);

			log.append("Remote read (sid=" + sctx.sid + ", requestVersion="
					+ sctx.requestVersion + ")\n");
			
			LOGGER.debug("REMOTE READ " + group + " " + metadata);

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

		log.append("Response (lastCommitted=" + read.lastCommitted
				+ ", mostRecent=" + read.mostRecent + ")\n");

		if (sctx.isUpdate() && !read.mostRecent)
		{
			LOGGER.debug("* onTxRead ABORT TRANSACTION !!! "
					+ "(throw TransactionException)\n" + sctx.trxID);
			throw new TransactionException();
		}
		// added to read set in onReadAccess context method

		log.append("------------------------------------------");
		LOGGER.debug(log.toString());

		PRProfiler.onTxCompleteReadFinish(ctx.threadID);
		return read.value;
	}

	private ReadDone doRead(int sid, ObjectMetadata metadata)
	{
		int origNextId;
		do {
			origNextId = nextId.get();
		} while (!nextId.compareAndSet(origNextId, Math.max(origNextId, sid)));

		VBoxField field = (VBoxField) TribuDSTM.getObject(metadata);

		long st = System.nanoTime();
		while (commitId.get() < sid
				&& !((InPlaceRWLock) field).isExclusiveUnlocked())
		{ // wait until (commitId.get() >= sid || ((InPlaceRWLock)
			// field).isExclusiveUnlocked()
			LOGGER.debug((commitId.get() < sid) + " "
					+ !((InPlaceRWLock) field).isExclusiveUnlocked());
		}
		long end = System.nanoTime();
		PRProfiler.onWaitingReadFinish(end - st);

		Version ver = field.getLastVersion().get(sid);
		boolean mostRecent = ver.equals(field.getLastVersion());
		int lastCommitted = commitId.get();

		// StringBuffer log = new StringBuffer();
		// log.append("------------------------------------------\n");
		// log.append("doRead (sid=" + sid + ")\n");
		// log.append("Metadata= " + metadata + "\n");
		// log.append("(lastCommitted=" + lastCommitted + ", mostRecent="
		// + mostRecent + ")\n");
		// log.append("------------------------------------------");
		// LOGGER.debug(log.toString());

		return new ReadDone(ver.value, lastCommitted, mostRecent);
	}

	private void updateNodeTimestamps(int lastCommitted)
	{
		int origNextId;
		do {
			origNextId = nextId.get();
		} while (!nextId.compareAndSet(origNextId,
				Math.max(origNextId, lastCommitted)));

		int origMaxSeenId;
		do {
			origMaxSeenId = maxSeenId.get();
		} while (!maxSeenId.compareAndSet(origMaxSeenId,
				Math.max(origMaxSeenId, lastCommitted)));
		advanceCommitId();

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		log.append("updateNodeTimestamps\n");
		log.append("nextId= " + nextId.get() + " (old=" + origNextId + ")\n");
		log.append("maxSeenId= " + maxSeenId.get() + " (old=" + origMaxSeenId
				+ ")\n");
		log.append("------------------------------------------");
		LOGGER.debug(log.toString());
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
		LOGGER.debug("* onDelivery (src=" + src + ") -> READ REQ");

		int newReadSid = msg.readSid;

		if (msg.firstRead && commitId.get() > msg.readSid)
		{
			newReadSid = commitId.get();
		}

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		log.append("readRequest (newReadSid=" + newReadSid
				+ ", requestVersion=" + msg.msgVersion + ")\n");
		log.append("Metadata= " + msg.metadata + "\n");
		log.append("firstRead= " + msg.firstRead + "\n");
		log.append("------------------------------------------");
		LOGGER.debug(log.toString());

		ReadDone read = doRead(newReadSid, msg.metadata);

		ReadRet ret = new ReadRet(msg.ctxID, msg.msgVersion, read);

		PRProfiler.onSerializationBegin(msg.ctxID);
		serializationContext.set(true);
		byte[] payload = ObjectSerializer.object2ByteArray(ret); // XXX read ctx
		PRProfiler.onSerializationFinish(msg.ctxID);

		PRProfiler.newMsgSent(payload.length);
		TribuDSTM.sendTo(payload, src);
		serializationContext.set(false);
		updateNodeTimestamps(msg.readSid);
	}

	private void readReturn(ReadRet msg, Address src)
	{ // I am the coordinator of this read.
		SCOReContext sctx = ((SCOReContext) ctxs.get(msg.ctxID));

		LOGGER.debug("* onDelivery (src=" + src + ") -> READ RET\n"
				+ sctx.trxID);

		if (sctx.requestVersion > msg.msgVersion)
		{ // read request already responded. ignore late responses
			return;
		}
		sctx.requestVersion++;
		sctx.response = msg.read;

		updateNodeTimestamps(msg.read.lastCommitted);

		LOGGER.debug("* readReturn (src=" + src + ")");

		sctx.syncMsg.release();
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
			// readRequest((ReadReq) obj, src);
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
		LOGGER.debug("* onDelivery (src=" + src + ") -> PREP MSG\n" + trxID);

		if (rejectTrxs.contains(trxID))
		{ // late PREPARE msg (already received DECIDE msg (NO) for this tx)
			LOGGER.debug("* prepareMessage LATE PREP MSG " + trxID);

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

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		log.append("prepareMessage (src=" + src + ") " + ctxID + ":"
				+ ctx.atomicBlockId + ":" + trxID + "\n");
		log.append("exclusiveLocks= " + exclusiveLocks + "\nsharedLocks= "
				+ sharedLocks + "\nvalidate= " + validate + "\n");
		log.append("Send VOTE msg (res=" + outcome + ", fsn=" + next + ")\n");
		log.append("------------------------------------------");
		LOGGER.debug(log.toString());

		VoteMsg vote = new VoteMsg(outcome, next, ctxID, trxID);

		PRProfiler.onSerializationBegin(ctxID);
		byte[] payload = ObjectSerializer.object2ByteArray(vote);
		PRProfiler.onSerializationFinish(ctxID);

		PRProfiler.newMsgSent(payload.length);
		TribuDSTM.sendTo(payload, src);
	}

	private void voteMessage(VoteMsg msg, Address src)
	{ // I am the coordinator of this commit. Gathering votes
		// this context is local. access directly
		SCOReContext ctx = (SCOReContext) ctxs.get(msg.ctxID);

		final String trxID_msg = msg.trxID;
		final String trxID = ctx.trxID;
		LOGGER.debug("* onDelivery (src=" + src + ") -> VOTE MSG\n" + trxID_msg);

		if (rejectTrxs.contains(trxID) || !trxID_msg.equals(trxID))
		{ // late vote. trx already aborted (no decide msg received yet)
			LOGGER.debug("* VoteMessage LATE VOTE " + trxID_msg);

			return;
		}

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		log.append("voteMessage (src=" + src + ") " + ctx.threadID + ":"
				+ ctx.atomicBlockId + ":" + trxID + "\n");
		final boolean outcome = msg.outcome;
		final List<Integer> votes = ctx.votes;
		final int proposedTimestamp = msg.proposedTimestamp;
		final int expectedVotes = ctx.expectedVotes;
		if (outcome)
		{
			log.append("outcome= YES fsn= " + proposedTimestamp + " ("
					+ (votes.size() + 1) + "/" + expectedVotes
					+ " votes)\n");
		}
		else
		{
			log.append("outcome= NO\n");
		}
		log.append("------------------------------------------");
		LOGGER.debug(log.toString());

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

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		log.append("finalizeVoteStep " + ctx.threadID + ":" + ctx.atomicBlockId
				+ ":" + ctx.trxID + "\n");
		log.append("outcome= " + outcome + " finalSid= " + finalSid + "\n");
		log.append("Send DECIDE msg to= " + group + "\n");
		log.append("------------------------------------------");
		LOGGER.debug(log.toString());

		PRProfiler.onSerializationBegin(ctx.threadID);
		byte[] payload = ObjectSerializer.object2ByteArray(decide);
		PRProfiler.onSerializationFinish(ctx.threadID);

		PRProfiler.newMsgSent(payload.length);
		TribuDSTM.sendToGroup(payload, group);
	}

	private synchronized void decideMessage(DecideMsg msg, Address src)
	{ // I am a participant in this commit. *atomically*
		final String trxID = msg.trxID;
		LOGGER.debug("* onDelivery (src=" + src + ") -> DECIDE MSG\n"
				+ trxID);

		final boolean result = msg.result;
		final int finalSid = msg.finalSid;
		if (result)
		{ // DECIDE YES
			int origNextId;
			do {
				origNextId = nextId.get();
			} while (!nextId.compareAndSet(origNextId, Math.max(origNextId, finalSid)));
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

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		try{
		log.append("decideMessage (src=" + src + ") " + ctxID + ":"
				+ tx.atomicBlockId + ":" + trxID + "\n");
		}catch (NullPointerException e)
		{
			log.append("decideMessage\n");
		}
		log.append("result= " + result + " finalSid= " + finalSid
				+ "\n");
		log.append("------------------------------------------");
		LOGGER.debug(log.toString());
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
				if (snId != -1) {
					// it is safe to make snapshot visible
					commitId.set(snId);
				}
				StringBuffer log = new StringBuffer(
						"* processTx stableQ empty\n");
				log.append(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
				log.append("stableQ: " + stableQ + "\n");
				log.append("pendQ: " + pendQ + "\n");
				log.append("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
				LOGGER.debug(log.toString());

				advanceCommitId();

				return;
			}

			Pair<String, Integer> sTx = stableQ.peek();
			Pair<String, Integer> pTx = pendQ.peek();

			if (pTx != null && pTx.second <= sTx.second)
			{ // there are still some trxs that can be serialized before/with sTx
				if (snId != -1) {
					// it is safe to make snapshot visible
					commitId.set(snId);
				}
				StringBuffer log = new StringBuffer(
						"* processTx some txs can be serialized before/with stableQ.head\n");
				log.append(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
				log.append("stableQ: " + stableQ + "\n");
				log.append("pendQ: " + pendQ + "\n");
				log.append("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
				LOGGER.debug(log.toString());

				return;
			}

			StringBuffer log = new StringBuffer();
			log.append(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
			log.append("stableQ: " + stableQ + "\n");
			log.append("pendQ: " + pendQ + "\n");
			log.append("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
			LOGGER.debug(log.toString());

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
			if (ctx.sid > snId) {
				// this snapshot is fresher than snId, it is safe to make
				// previous snapshot visible
				commitId.set(snId);
			}
			snId = ctx.sid;

			ctx.applyWriteSet();

			((SCOReReadSet) tx.rs).releaseSharedLocks(sTx.first);
			((SCOReWriteSet) tx.ws).releaseExclusiveLocks(sTx.first);

			stableQ.poll(); // remove sTx
			receivedTrxs.remove(sTx.first);

			log = new StringBuffer();
			log.append("++++++++++++++++++++++++++++++++++++++++\n");
			log.append("TRANSACTION COMMITTED!!!\n");
			log.append("processTx " + ctx.threadID + ":" + ctx.atomicBlockId
					+ ":" + ctx.trxID + "\n");
			log.append("finalSid= " + ctx.sid + "\n");
			log.append("nextId= " + nextId.get() + " commitId= " + sTx.second
					+ " maxSeenId= " + maxSeenId.get() + "\n");
			log.append("stableQ: " + stableQ + "\n");
			log.append("pendQ: " + pendQ + "\n");
			log.append("++++++++++++++++++++++++++++++++++++++++++");
			LOGGER.debug(log.toString());

			ctx.processed(true);
		}
	}

	final private void advanceCommitId() {
		int origCommitId;
		do {
			origCommitId = commitId.get();
			if (origCommitId >= maxSeenId.get() || !pendQ.isEmpty()
					|| !stableQ.isEmpty()) {
				return;
			}
		} while (!commitId.compareAndSet(origCommitId, maxSeenId.get()));
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
