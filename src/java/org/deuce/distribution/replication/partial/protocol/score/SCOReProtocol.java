package org.deuce.distribution.replication.partial.protocol.score;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
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

	private final AtomicInteger commitId = new AtomicInteger(0);
	private final AtomicInteger nextId = new AtomicInteger(0);
	private final AtomicInteger maxSeenId = new AtomicInteger(0);

	private final int timeout = Integer.getInteger(
			"tribu.distributed.protocol.score.timeout", 3000);
	private final Timer timeoutTimer = new Timer();

	private final Map<Integer, DistributedContext> ctxs = Collections
			.synchronizedMap(new HashMap<Integer, DistributedContext>());

	private final BlockingQueue<Pair<String, Integer>> pendQ = new PriorityBlockingQueue<Pair<String, Integer>>(
			50, comp);
	private final BlockingQueue<Pair<String, Integer>> stableQ = new PriorityBlockingQueue<Pair<String, Integer>>(
			50, comp);

	private final Map<String, DistributedContextState> receivedTrxs = Collections
			.synchronizedMap(new HashMap<String, DistributedContextState>());
	private final Set<String> rejectTrxs = Collections
			.synchronizedSet(new HashSet<String>());

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

		TimerTask task = createTimeoutHandler(sctx);
		sctx.timeoutTask = task;
		timeoutTimer.schedule(task, timeout); // set timeout

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		log.append("onTxCommit " + sctx.threadID + ":" + sctx.atomicBlockId
				+ ":" + sctx.trxID + "\n");
		log.append("Coordinator= " + ((SCOReContextState) ctxState).src
				+ " sid= " + sctx.sid + "\n");
		log.append("Send PREP msg to= " + resGroup + "\n");
		String t = Integer.toHexString(System.identityHashCode(task));
		log.append("Timeout (" + t + ")= " + timeout + "ms\n");
		log.append("WS: " + sctx.writeSet + "\n");
		log.append("RS: " + sctx.readSet + "\n");
		log.append("------------------------------------------");
		LOGGER.debug(log.toString());

		PRProfiler.onSerializationBegin(ctx.threadID);

		byte[] payload = ObjectSerializer.object2ByteArray(ctxState);

		PRProfiler.onSerializationFinish(ctx.threadID);

		PRProfiler.onPrepSend(ctx.threadID);
		PRProfiler.newMsgSent(payload.length);

		TribuDSTM.sendTotalOrdered(payload, resGroup);
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
					PRProfiler.txTimeout();

					StringBuffer log = new StringBuffer();
					log.append("------------------------------------------\n");
					String t = Integer.toHexString(System
							.identityHashCode(this));
					log.append("Timeout (" + t + ") triggered\n" + ctx.threadID
							+ ":" + ctx.atomicBlockId + ":" + ctx.trxID + "\n");
					log.append("ABORT TRANSACTION !!!\n");
					log.append("------------------------------------------");
					LOGGER.debug(log.toString());

					finalizeVoteStep(ctx, false);
				}
			}
		};
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

			if (firstRead)
			{ // first read of this transaction
				sctx.sid = read.lastCommitted;
			}
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
		int max = Math.max(nextId.get(), sid);
		nextId.set(max);

		VBoxField field = (VBoxField) TribuDSTM.getObject(metadata);

		while (commitId.get() < sid
				&& !((InPlaceRWLock) field).isExclusiveUnlocked())
		{ // wait until (commitId.get() >= sid || ((InPlaceRWLock)
			// field).isExclusiveUnlocked()
			LOGGER.debug((commitId.get() < sid) + " "
					+ !((InPlaceRWLock) field).isExclusiveUnlocked());
		}

		Version ver = field.getLastVersion().get(sid);
		boolean mostRecent = ver.equals(field.getLastVersion());
		int lastCommitted = commitId.get();

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		log.append("doRead (sid=" + sid + ")\n");
		log.append("Metadata= " + metadata + "\n");
		log.append("(lastCommitted=" + lastCommitted + ", mostRecent="
				+ mostRecent + ")\n");
		log.append("------------------------------------------");
		// LOGGER.debug(log.toString());

		return new ReadDone(ver.value, lastCommitted, mostRecent);
	}

	private void updateNodeTimestamps(int lastCommitted)
	{
		int oldNextId = nextId.get();
		int max = Math.max(oldNextId, lastCommitted);
		nextId.set(max);

		int oldMaxSeenId = maxSeenId.get();
		int maxSeen = Math.max(oldMaxSeenId, lastCommitted);
		maxSeenId.set(maxSeen);

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		log.append("updateNodeTimestamps\n");
		log.append("nextId= " + nextId.get() + "(old=" + oldNextId + ")\n");
		log.append("maxSeenId= " + maxSeenId.get() + "(old=" + oldMaxSeenId
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
		serializationContext.set(false);

		PRProfiler.onSerializationFinish(msg.ctxID);

		PRProfiler.newMsgSent(payload.length);

		TribuDSTM.sendTo(payload, src);
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
		LOGGER.debug("* onDelivery (src=" + src + ") -> PREP MSG\n" + ctx.trxID);

		if (rejectTrxs.contains(ctx.trxID))
		{ // late PREPARE msg (already received DECIDE msg (NO) for this tx)
			LOGGER.debug("* prepareMessage LATE PREP MSG " + ctx.trxID);

			// rejectTrxs.remove(ctx.trxID); XXX
			return;
		}

		receivedTrxs.put(ctx.trxID, ctx);

		PRProfiler.onTxValidateBegin(ctx.ctxID);

		boolean exclusiveLocks = ((SCOReWriteSet) ctx.ws)
				.getExclusiveLocks(ctx.trxID);
		boolean sharedLocks = ((SCOReReadSet) ctx.rs).getSharedLocks(ctx.trxID);
		boolean validate = ((SCOReReadSet) ctx.rs).validate(ctx.sid);

		PRProfiler.onTxValidateEnd(ctx.ctxID);

		boolean outcome = exclusiveLocks && sharedLocks && validate;

		int next = -1;
		if (outcome) // valid trx
		{
			next = nextId.incrementAndGet();
			pendQ.add(new Pair<String, Integer>(ctx.trxID, next));
		}
		else
		{ // vote NO! do not need the locks
			if (sharedLocks)
			{
				((SCOReReadSet) ctx.rs).releaseSharedLocks(ctx.trxID);
			}
			if (exclusiveLocks)
			{
				((SCOReWriteSet) ctx.ws).releaseExclusiveLocks(ctx.trxID);
			}
		}

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		log.append("prepareMessage (src=" + src + ") " + ctx.ctxID + ":"
				+ ctx.atomicBlockId + ":" + ctx.trxID + "\n");
		log.append("exclusiveLocks= " + exclusiveLocks + "\nsharedLocks= "
				+ sharedLocks + "\nvalidate= " + validate + "\n");
		log.append("Send VOTE msg (res=" + outcome + ", fsn=" + next + ")\n");
		log.append("------------------------------------------");
		LOGGER.debug(log.toString());

		VoteMsg vote = new VoteMsg(outcome, next, ctx.ctxID, ctx.trxID);

		PRProfiler.onSerializationBegin(ctx.ctxID);

		byte[] payload = ObjectSerializer.object2ByteArray(vote);

		PRProfiler.onSerializationFinish(ctx.ctxID);

		PRProfiler.newMsgSent(payload.length);

		TribuDSTM.sendTo(payload, src);
	}

	private void voteMessage(VoteMsg msg, Address src)
	{ // I am the coordinator of this commit. Gathering votes
		// this context is local. access directly
		SCOReContext ctx = (SCOReContext) ctxs.get(msg.ctxID);

		LOGGER.debug("* onDelivery (src=" + src + ") -> VOTE MSG\n" + msg.trxID);

		if (rejectTrxs.contains(ctx.trxID) || !msg.trxID.equals(ctx.trxID))
		{ // late vote. trx already aborted (no decide msg received yet)
			LOGGER.debug("* VoteMessage LATE VOTE " + msg.trxID);

			return;
		}

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		log.append("voteMessage (src=" + src + ") " + ctx.threadID + ":"
				+ ctx.atomicBlockId + ":" + ctx.trxID + "\n");
		if (msg.outcome)
		{
			log.append("outcome= YES fsn= " + msg.proposedTimestamp + " ("
					+ (ctx.votes.size() + 1) + "/" + ctx.expectedVotes
					+ " votes)\n");
		}
		else
		{
			log.append("outcome= NO\n");
		}
		log.append("------------------------------------------");
		LOGGER.debug(log.toString());

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
				PRProfiler.onLastVoteReceived(ctx.threadID);

				ctx.timeoutTask.cancel(); // cancel timeout
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
		LOGGER.debug("* onDelivery (src=" + src + ") -> DECIDE MSG\n"
				+ msg.trxID);

		if (msg.result)
		{ // DECIDE YES
			int max = Math.max(nextId.get(), msg.finalSid);
			nextId.set(max);
			stableQ.add(new Pair<String, Integer>(msg.trxID, msg.finalSid));
		}

		boolean remove = pendQ.remove(new Pair<String, Integer>(msg.trxID, -1));

		if (!msg.result)
		{ // DECIDE NO
			SCOReContextState tx = (SCOReContextState) receivedTrxs
					.get(msg.trxID);

			if (tx != null)
			{ // received DECIDE msg *after* PREPARE msg (someone voted NO)
				if (remove) // I only have the locks if I voted YES
				{ // and put the tx in the pendQ
					((SCOReReadSet) tx.rs).releaseSharedLocks(msg.trxID);
					((SCOReWriteSet) tx.ws).releaseExclusiveLocks(msg.trxID);
				}

				SCOReContext ctx = null;
				if (src.isLocal())
				{ // context is local. access directly
					ctx = (SCOReContext) ctxs.get(msg.ctxID);
					ctx.processed(false);
				}

				receivedTrxs.remove(msg.trxID);
				// rejectTrxs.remove(msg.trxID); XXX
			}
			else
			{ // received DECIDE msg *before* PREPARE msg (someone voted NO)
				if (src.isLocal())
				{ // context is local. access directly
					SCOReContext ctx = (SCOReContext) ctxs.get(msg.ctxID);
					ctx.processed(false);
				}

				rejectTrxs.add(msg.trxID);
			}
		}

		StringBuffer log = new StringBuffer();
		log.append("------------------------------------------\n");
		log.append("decideMessage (src=" + src + ") " + msg.ctxID + ":_:"
				+ msg.trxID + "\n");
		log.append("result= " + msg.result + " finalSid= " + msg.finalSid
				+ "\n");
		log.append("------------------------------------------");
		LOGGER.debug(log.toString());
	}

	private synchronized void processTx()
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
				StringBuffer log = new StringBuffer(
						"* processTx stableQ empty\n");
				log.append(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
				log.append("stableQ: " + stableQ + "\n");
				log.append("pendQ: " + pendQ + "\n");
				log.append("<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<");
				LOGGER.debug(log.toString());

				return;
			}

			Pair<String, Integer> sTx = stableQ.peek();
			Pair<String, Integer> pTx = pendQ.peek();

			if (pTx != null && pTx.second < sTx.second)
			{ // there are still some trxs that can be serialized before sTx
				StringBuffer log = new StringBuffer(
						"* processTx some txs can be serialized before stableQ.head\n");
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

			commitId.set(sTx.second);
			ctx.processed(true);
		}
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
