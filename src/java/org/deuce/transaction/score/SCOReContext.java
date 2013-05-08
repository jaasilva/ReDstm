package org.deuce.transaction.score;

import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;

import org.deuce.LocalMetadata;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.oid.PartialReplicationOID;
import org.deuce.distribution.replication.partial.protocol.score.ReadDone;
import org.deuce.profiling.Profiler;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.pool.Pool;
import org.deuce.transaction.pool.ResourceFactory;
import org.deuce.transaction.score.field.SCOReReadFieldAccess;
import org.deuce.transaction.score.field.SCOReWriteFieldAccess;
import org.deuce.transaction.score.field.VBoxField;
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
 * 
 */
@ExcludeTM
@LocalMetadata(metadataClass = "org.deuce.transaction.score.field.VBoxField")
public class SCOReContext extends DistributedContext
{
	public static final TransactionException VERSION_UNAVAILABLE_EXCEPTION = new TransactionException(
			"Fail on retrieveing an older or unexistent version.");

	protected SCOReReadSet readSet;
	protected SCOReWriteSet writeSet;

	public int sid;
	public boolean firstReadDone;
	public List<Integer> votes;
	public int expectedVotes;
	public String trxID;
	private Group involvedNodes;

	public TimerTask timeoutTask;
	public Semaphore syncMsg;
	public ReadDone response;
	public int requestVersion;

	public SCOReContext()
	{
		super();
		readSet = createReadSet();
		writeSet = createWriteSet();

		syncMsg = new Semaphore(0);
		response = null;
		involvedNodes = null;
		requestVersion = 0;
	}

	private SCOReReadSet createReadSet()
	{
		return new SCOReReadSet();
	}

	private SCOReWriteSet createWriteSet()
	{
		return new SCOReWriteSet();
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
		curr.init((VBoxField) field);

		return writeSet.contains(curr);
	}

	private Object read(Object value, TxField field)
	{
		SCOReWriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
		{ // not in the writeSet. Do distributed read
			return TribuDSTM.onTxRead(this, field.getMetadata());
		}
		else
		{ // in the writeSet. Return value
			return writeAccess.getValue();
		}
	}

	@Override
	public ArrayContainer onReadAccess(ArrayContainer value, TxField field)
	{
		return (ArrayContainer) read(value, field);
	}

	@Override
	public Object onReadAccess(Object value, TxField field)
	{
		return read(value, field);
	}

	@Override
	public boolean onReadAccess(boolean value, TxField field)
	{
		return (Boolean) read(value, field);
	}

	@Override
	public byte onReadAccess(byte value, TxField field)
	{
		return (Byte) read(value, field);
	}

	@Override
	public char onReadAccess(char value, TxField field)
	{
		return (Character) read(value, field);
	}

	@Override
	public short onReadAccess(short value, TxField field)
	{
		return (Short) read(value, field);
	}

	@Override
	public int onReadAccess(int value, TxField field)
	{
		return (Integer) read(value, field);
	}

	@Override
	public long onReadAccess(long value, TxField field)
	{
		return (Long) read(value, field);
	}

	@Override
	public float onReadAccess(float value, TxField field)
	{
		return (Float) read(value, field);
	}

	@Override
	public double onReadAccess(double value, TxField field)
	{
		return (Double) read(value, field);
	}

	private void addWriteAccess(SCOReWriteFieldAccess write)
	{ // add to writeSet
		writeSet.put(write);
	}

	private void checkGroupRestrictions(UniqueObject obj, TxField field)
	{
		Group txFieldGroup = ((PartialReplicationOID) field.getMetadata())
				.getGroup();
		PartialReplicationOID objMetadata = (PartialReplicationOID) obj
				.getMetadata();

		if (objMetadata == null)
		{ // normal object, no metadata (no TxField). first time written.
			objMetadata = (PartialReplicationOID) TribuDSTM
					.getObjectSerializer().createMetadata();
			// group not defined. assign the same group as the txField
			objMetadata.setGroup(txFieldGroup);
			obj.setMetadata(objMetadata);
		}
		else
		{
			Group objGroup = objMetadata.getGroup();

			if (objGroup == null) // group not defined
			{ // assign the same group as the txField
				objMetadata.setGroup(txFieldGroup);
			}
			else if (!txFieldGroup.equals(objGroup))
			{ // different group. cannot happen (for now)
				throw new TransactionException();
			}
		}
	}

	@Override
	public void onWriteAccess(ArrayContainer value, TxField field)
	{
		if (value != null)
		{
			checkGroupRestrictions(value, field);
		}

		SCOReWriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(Object value, TxField field)
	{
		if (value != null)
		{
			checkGroupRestrictions((UniqueObject) value, field);
		}

		SCOReWriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(boolean value, TxField field)
	{
		SCOReWriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(byte value, TxField field)
	{
		SCOReWriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(char value, TxField field)
	{
		SCOReWriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(short value, TxField field)
	{
		SCOReWriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(int value, TxField field)
	{
		SCOReWriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(long value, TxField field)
	{
		SCOReWriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(float value, TxField field)
	{
		SCOReWriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(double value, TxField field)
	{
		SCOReWriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
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
		this.votes = null;
		this.expectedVotes = 0;
		this.timeoutTask = null;
		response = null;
		trxID = java.util.UUID.randomUUID().toString();
		involvedNodes = null;
		syncMsg = new Semaphore(0); // this should not be necessary

		WFAPool.clear();
	}

	@Override
	protected boolean performValidation()
	{
		return readSet.validate(sid);
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
		profiler.onTxAppCommit();

		if (writeSet.isEmpty())
		{ // read-only transaction
			if (Profiler.enabled)
				profiler.txCommitted++;

			TribuDSTM.onTxFinished(this, true);
			return true;
		}

		TribuDSTM.onTxCommit(this);
		try
		{ // blocked awaiting distributed validation
			trxProcessed.acquire();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

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