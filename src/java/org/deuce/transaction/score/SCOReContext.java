package org.deuce.transaction.score;

import java.util.concurrent.Semaphore;

import org.deuce.Defaults;
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
import org.deuce.transaction.score.field.SCOReReadFieldAccess;
import org.deuce.transaction.score.field.SCOReWriteFieldAccess;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ArrayContainer;
import org.deuce.transform.localmetadata.type.TxField;

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
public class SCOReContext extends DistributedContext
{
	public static final TransactionException VERSION_UNAVAILABLE_EXCEPTION = new TransactionException(
			"Fail on retrieveing an older or unexistent version.");
	public static int MAX_VERSIONS = Integer
			.getInteger(Defaults._SCORE_MVCC_MAX_VERSIONS,
					Defaults.SCORE_MVCC_MAX_VERSIONS);

	public SCOReReadSet readSet;
	public SCOReWriteSet writeSet;

	public int sid;
	public boolean firstReadDone;
	public int maxVote;
	public int receivedVotes;
	public int expectedVotes;
	public String trxID;
	private Group involvedNodes;

	public Semaphore syncMsg;
	public ReadDone response;
	public int requestVersion;

	public SCOReContext()
	{
		super();
		readSet = new SCOReReadSet();
		writeSet = new SCOReWriteSet();

		syncMsg = new Semaphore(0);
		response = null;
		involvedNodes = null;
		requestVersion = 0;
	}

	@Override
	public void onIrrevocableAccess()
	{
	}

	@Override
	public void beforeReadAccess(TxField field)
	{
	}

	private SCOReWriteFieldAccess onReadAccess(TxField field)
	{
		SCOReReadFieldAccess curr = readSet.getNext();
		curr.init(field);

		return writeSet.contains(curr);
	}

	private Object read(TxField field)
	{
		SCOReWriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
		{ // *NOT* in the writeSet. Do distributed read
			return TribuDSTM.onTxRead(this, field);
		}
		else
		{ // *IN* the writeSet. Return value
			Profiler.onTxWsRead(threadID);
			return writeAccess.getValue();
		}
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
			objGroup.set(txFieldPGroup.getAll());
			objPGroup.set(txFieldPGroup.getAll());
		}
		else if (!txFieldMetadata.isPublished() && objMetadata.isPublished())
		{ // ~published(f) && published(o)
			txFieldPGroup.set(objGroup.getAll());
		}
		else
		{ // ~published(f) && ~published(o)
			// txFieldPGroup.set(objGroup.getAll());
			Group objPGroup = objMetadata.getPartialGroup();
			objGroup.set(txFieldPGroup.getAll());
			objPGroup.set(txFieldPGroup.getAll());
		}
	}

	private void addWriteAccess(SCOReWriteFieldAccess write)
	{ // add to writeSet
		writeSet.put(write);
	}

	private void write(Object value, TxField field)
	{
		SCOReWriteFieldAccess next = WFAPool.getNext();
		next.set(value, field);
		addWriteAccess(next);
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
		if (value != null && !(value instanceof String)
				&& !(value instanceof Byte) && !(value instanceof Short)
				&& !(value instanceof Integer) && !(value instanceof Long)
				&& !(value instanceof Float) && !(value instanceof Double)
				&& !(value instanceof Character) && !(value instanceof Boolean)
				&& (value instanceof UniqueObject))
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

	@Override
	public DistributedContextState createState()
	{
		return new SCOReContextState(readSet, writeSet, threadID,
				atomicBlockId, sid, trxID);
	}

	@Override
	protected void initialise(int atomicBlockId, String metainf)
	{
		readSet.clear();
		writeSet.clear();

		this.sid = 0;
		this.firstReadDone = false;
		this.maxVote = -1;
		this.receivedVotes = 0;
		this.expectedVotes = 0;
		response = null;
		trxID = java.util.UUID.randomUUID().toString();
		involvedNodes = null;
		syncMsg = new Semaphore(0); // this should not be necessary

		WFAPool.clear();
	}

	@Override
	protected boolean performValidation()
	{
		boolean exclusiveLocks = false, sharedLocks = false, validate = false;
		exclusiveLocks = writeSet.getExclusiveLocks(trxID);
		if (exclusiveLocks) // only continue validation if I can succeed
		{
			sharedLocks = readSet.getSharedLocks(trxID);
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
				readSet.releaseSharedLocks(trxID);
			}
			if (exclusiveLocks)
			{
				writeSet.releaseExclusiveLocks(trxID);
			}
		}

		return outcome;
	}

	@Override
	protected void applyUpdates()
	{
		writeSet.apply(sid);
	}

	public void recreateContextFromState(DistributedContextState ctxState)
	{
		super.recreateContextFromState(ctxState);
		atomicBlockId = -1;

		SCOReContextState sctx = (SCOReContextState) ctxState;

		readSet = (SCOReReadSet) ctxState.rs;
		writeSet = (SCOReWriteSet) ctxState.ws;

		sid = sctx.sid;
		trxID = sctx.trxID;

		WFAPool.clear();
	}

	public boolean isUpdate()
	{
		return !writeSet.isEmpty();
	}

	public void unlock()
	{
		readSet.releaseSharedLocks(trxID);
		writeSet.releaseExclusiveLocks(trxID);
	}

	public Group getInvolvedNodes()
	{ // !!! be careful when to call this
		if (involvedNodes == null)
		{
			Group group1 = readSet.getInvolvedNodes();
			Group group2 = writeSet.getInvolvedNodes();
			involvedNodes = group1.union(group2);
		}
		return involvedNodes;
	}

	public boolean commit()
	{
		Profiler.onTxAppFinish(threadID);

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
			ResourceFactory<SCOReWriteFieldAccess>
	{
		public SCOReWriteFieldAccess newInstance()
		{
			return new SCOReWriteFieldAccess();
		}
	}

	final private Pool<SCOReWriteFieldAccess> WFAPool = new Pool<SCOReWriteFieldAccess>(
			new WFAResourceFactory());
}
