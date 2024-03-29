package org.deuce.transaction.score;

import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.LocalMetadata;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.PartialReplicationOID;
import org.deuce.distribution.replication.partial.protocol.score.msgs.ReadDone;
import org.deuce.profiling.Profiler;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.GroupsViolationException;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.pool.Pool;
import org.deuce.transaction.pool.ResourceFactory;
import org.deuce.transaction.score.field.InPlaceRWLock;
import org.deuce.transaction.score.field.ReadFieldAccess;
import org.deuce.transaction.score.field.WriteFieldAccess;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transaction.score.field.Version;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ArrayContainer;
import org.deuce.transform.localmetadata.type.TxField;
import org.deuce.trove.TObjectProcedure;

/**
 * Snapshot visibility for transactions is determined by associating to each
 * transaction T a scalar timestamp, which we call snapshot identifier (sid).
 * The sid of a transaction is established upon its first read operation. In
 * this case, the most recent version of the requested datum is returned, and
 * the transaction's sid is set to the value of commitId at the transaction's
 * originating node, if the read can be served locally. Otherwise, if the
 * requested datum is not maintained locally, T.sid is set equal to the maximum
 * between commitId at the originating node and commitId at the remote node from
 * which T read. From that moment on, any subsequent read operation is allowed
 * to observe the most recent committed version of the requested datum having
 * timestamp less or equal to T.sid, as in classical MVCC algorithms.
 * 
 * @author jaasilva
 */
@ExcludeTM
@LocalMetadata(metadataClass = "org.deuce.transaction.score.field.VBoxField")
public class Context extends DistributedContext
{
	public static final TransactionException OVERWRITTEN_VERSION_EXCEPTION = new TransactionException(
			"Forced to see overwritten data.");

	// updated ONLY by bottom threads
	public static final AtomicInteger commitId = new AtomicInteger(0);
	// updated by up and bottom threads
	public static final AtomicInteger nextId = new AtomicInteger(0);
	// updated ONLY by bottom threads
	public static final AtomicInteger maxSeenId = new AtomicInteger(0);

	private ReadSet readSet;
	private WriteSet writeSet;

	public int sid;
	public boolean firstReadDone;
	public int maxVote;
	public int receivedVotes;
	public int expectedVotes;
	public String txnID;
	private Group involvedNodes;

	public Semaphore syncMsg;
	public ReadDone response;
	public int requestVersion;

	public Context()
	{
		super();
		readSet = new ReadSet();
		writeSet = new WriteSet();

		syncMsg = new Semaphore(0);
		response = null;
		involvedNodes = null;
		requestVersion = 0;
	}

	public Set<WriteFieldAccess> getCommittedKeys()
	{
		return writeSet.getWrites();
	}

	@Override
	public void onIrrevocableAccess()
	{
	}

	@Override
	public void beforeReadAccess(TxField field)
	{
	}

	/***************************************
	 * ON READ ACCESS
	 **************************************/

	private WriteFieldAccess onReadAccess(TxField field)
	{
		ReadFieldAccess curr = readSet.getNext();
		curr.init(field);
		return writeSet.contains(curr);
	}

	private Object read(TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess != null)
		{ // *IN* the writeSet. Return value
			Profiler.onTxWsRead(threadID);
			return writeAccess.getValue();
		}
		else
		{ // *NOT* in the writeSet. Do regular read operation
			if (!this.firstReadDone) // if this is the first read
			{
				this.sid = commitId.get();
			}

			ReadDone read = (ReadDone) TribuDSTM.onTxRead(this, field);

			if (!this.firstReadDone /* && read.mostRecent */)
			{ // advance our snapshot id to a fresher one
				this.sid = Math.max(sid, read.lastCommitted);
				// this.sid = read.lastCommitted;
			}

			if (this.isUpdate() && !read.mostRecent)
			{ // *optimization*: abort tx forced to see overwritten data
				throw OVERWRITTEN_VERSION_EXCEPTION;
			}

			if (!this.firstReadDone) // if this is the first read
			{
				this.firstReadDone = true;
			}

			return read.value; // XXX read == null sometimes
		}
	}

	public ReadDone doReadLocal(VBoxField field)
	{
		synchronized (nextId)
		{
			nextId.set(Math.max(nextId.get(), sid));
		}

		long st = System.nanoTime();
		while ((commitId.get() < sid)
				&& !((InPlaceRWLock) field).isExclusiveUnlocked())
		{/*
		 * wait until (commitId.get() >= sid || ((InPlaceRWLock)
		 * field).isExclusiveUnlocked())
		 */
		}
		long end = System.nanoTime();
		Profiler.onWaitingRead(end - st);

		Version ver = field.getLastVersion().get(sid);
		boolean mostRecent = ver.equals(field.getLastVersion());
		int lastCommitted = commitId.get();

		return new ReadDone(ver.value, lastCommitted, mostRecent);
	}

	@Override
	public ArrayContainer onReadAccess(ArrayContainer value, TxField field)
	{
		return (ArrayContainer) read(field);
	}

	@Override
	public Object onReadAccess(Object value, TxField field)
	{
		return read(field);
	}

	@Override
	public boolean onReadAccess(boolean value, TxField field)
	{
		return (Boolean) read(field);
	}

	@Override
	public byte onReadAccess(byte value, TxField field)
	{
		return (Byte) read(field);
	}

	@Override
	public char onReadAccess(char value, TxField field)
	{
		return (Character) read(field);
	}

	@Override
	public short onReadAccess(short value, TxField field)
	{
		return (Short) read(field);
	}

	@Override
	public int onReadAccess(int value, TxField field)
	{
		return (Integer) read(field);
	}

	@Override
	public long onReadAccess(long value, TxField field)
	{
		return (Long) read(field);
	}

	@Override
	public float onReadAccess(float value, TxField field)
	{
		return (Float) read(field);
	}

	@Override
	public double onReadAccess(double value, TxField field)
	{
		return (Double) read(field);
	}

	/***************************************
	 * ON WRITE ACCESS
	 **************************************/

	private void checkGroupRestrictions(UniqueObject obj, TxField field)
	{
		PartialReplicationOID txFieldMetadata = (PartialReplicationOID) field
				.getMetadata();
		PartialReplicationOID objMetadata = (PartialReplicationOID) obj
				.getMetadata();
		Group txFieldPGroup = txFieldMetadata.getPartialGroup();
		Group objGroup = objMetadata.getGroup();

		if (txFieldMetadata.isPublished() && objMetadata.isPublished())
		{ // published(f) && published(o)
			if (!txFieldPGroup.equals(objGroup))
			{ // different groups. cannot happen (for now...)
				throw new GroupsViolationException(txFieldPGroup + " != "
						+ objGroup);
			}
		}
		else if (txFieldMetadata.isPublished() && !objMetadata.isPublished())
		{ // published(f) && ~published(o)
			Group objPGroup = objMetadata.getPartialGroup();
			objGroup.set(txFieldPGroup.getMembers());
			objPGroup.set(txFieldPGroup.getMembers());
		}
		else if (!txFieldMetadata.isPublished() && objMetadata.isPublished())
		{ // ~published(f) && published(o)
			txFieldPGroup.set(objGroup.getMembers());
		}
		else
		{ // ~published(f) && ~published(o)
			// txFieldPGroup.set(objGroup.getAll());
			Group objPGroup = objMetadata.getPartialGroup();
			objGroup.set(txFieldPGroup.getMembers());
			objPGroup.set(txFieldPGroup.getMembers());
		}
	}

	private void write(Object value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, field);
		writeSet.put(next);
	}

	@Override
	public void onWriteAccess(ArrayContainer value, TxField field)
	{
		if (value != null)
		{
			checkGroupRestrictions(value, field);
		}

		write(value, field);
	}

	@Override
	public void onWriteAccess(Object value, TxField field)
	{
		if (value != null && (value instanceof UniqueObject)
				&& !(value instanceof String) && !(value instanceof Byte)
				&& !(value instanceof Short) && !(value instanceof Integer)
				&& !(value instanceof Long) && !(value instanceof Float)
				&& !(value instanceof Double) && !(value instanceof Character)
				&& !(value instanceof Boolean))
		{
			checkGroupRestrictions((UniqueObject) value, field);
		}

		write(value, field);
	}

	@Override
	public void onWriteAccess(boolean value, TxField field)
	{
		write(value, field);
	}

	@Override
	public void onWriteAccess(byte value, TxField field)
	{
		write(value, field);
	}

	@Override
	public void onWriteAccess(char value, TxField field)
	{
		write(value, field);
	}

	@Override
	public void onWriteAccess(short value, TxField field)
	{
		write(value, field);
	}

	@Override
	public void onWriteAccess(int value, TxField field)
	{
		write(value, field);
	}

	@Override
	public void onWriteAccess(long value, TxField field)
	{
		write(value, field);
	}

	@Override
	public void onWriteAccess(float value, TxField field)
	{
		write(value, field);
	}

	@Override
	public void onWriteAccess(double value, TxField field)
	{
		write(value, field);
	}

	/***************************************
	 * INHERITED METHODS
	 **************************************/

	@Override
	public DistributedContextState createState()
	{
		return new ContextState(readSet, writeSet, threadID, atomicBlockId,
				sid, txnID);
	}

	@Override
	protected void initialise(int atomicBlockId, String metainf)
	{
		this.readSet.clear();
		this.writeSet.clear();

		this.sid = 0;
		this.firstReadDone = false;
		this.maxVote = -1;
		this.receivedVotes = 0;
		this.expectedVotes = 0;
		this.response = null;
		this.txnID = java.util.UUID.randomUUID().toString();
		this.involvedNodes = null;
		// syncMsg = new Semaphore(0);

		this.WFAPool.clear();
	}

	@Override
	protected boolean performValidation()
	{
		boolean exclusiveLocks = false, sharedLocks = false, validate = false;
		exclusiveLocks = writeSet.getExclusiveLocks(txnID);
		if (exclusiveLocks) // only continue validation if I can succeed
		{
			sharedLocks = readSet.getSharedLocks(txnID);
		}
		if (sharedLocks)
		{
			validate = readSet.validate(sid);
		}
		boolean outcome = exclusiveLocks && sharedLocks && validate;

		if (!outcome)
		{ // vote NO! do not need the locks
			if (sharedLocks)
			{
				readSet.releaseSharedLocks(txnID);
			}
			if (exclusiveLocks)
			{
				writeSet.releaseExclusiveLocks(txnID);
			}
		}

		return outcome;
	}

	private final TObjectProcedure<WriteFieldAccess> putProcedure = new TObjectProcedure<WriteFieldAccess>()
	{
		@Override
		public boolean execute(WriteFieldAccess wfa)
		{
			PartialReplicationOID meta = (PartialReplicationOID) wfa.field
					.getMetadata();
			if (meta.getPartialGroup().isLocal())
			{
				wfa.put(sid);
			}
			return true;
		}
	};

	@Override
	protected void applyUpdates()
	{
		writeSet.forEach(putProcedure);
	}

	@Override
	public void recreateContextFromState(DistributedContextState ctxState)
	{
		super.recreateContextFromState(ctxState);
		atomicBlockId = -1;

		ContextState sctx = (ContextState) ctxState;

		readSet = (ReadSet) ctxState.rs;
		writeSet = (WriteSet) ctxState.ws;

		sid = sctx.sid;
		txnID = sctx.txnID;

		WFAPool.clear();
	}

	public boolean isUpdate()
	{
		return !writeSet.isEmpty();
	}

	public void unlock()
	{
		readSet.releaseSharedLocks(txnID);
		writeSet.releaseExclusiveLocks(txnID);
	}

	public Group getInvolvedNodes()
	{ // called on TxCommit CHECKME groups
		if (involvedNodes == null)
		{
			Group group1 = readSet.getInvolvedNodes();
			Group group2 = writeSet.getInvolvedNodes();
			involvedNodes = group1.union(group2);
			/*
			 * the local node is always involved in a transaction where it is
			 * the coordinator. the coordinator/local node needs to participate
			 * in this transaction's voting in order to release its context in
			 * the end.
			 */
			involvedNodes.add(TribuDSTM.getLocalAddress());
		}
		return involvedNodes;
	}

	@Override
	public boolean commit()
	{
		if (writeSet.isEmpty())
		{ // read-only transaction
			Profiler.txProcessed(true);

			TribuDSTM.onTxFinished(this, true);
			return true;
		}

		Profiler.onTxConfirmationBegin(threadID);
		TribuDSTM.onTxCommit(this);
		try
		{ // blocked awaiting distributed validation
			trxProcessed.acquire();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}
		Profiler.onTxConfirmationFinish(threadID);

		TribuDSTM.onTxFinished(this, committed);
		boolean result = committed;
		committed = false;
		return result;
	}

	private static class WFAResourceFactory implements
			ResourceFactory<WriteFieldAccess>
	{
		public WriteFieldAccess newInstance()
		{
			return new WriteFieldAccess();
		}
	}

	final private Pool<WriteFieldAccess> WFAPool = new Pool<WriteFieldAccess>(
			new WFAResourceFactory());
}
