package org.deuce.transaction.score;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;

import org.deuce.LocalMetadata;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.GroupUtils;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.score.field.ReadFieldAccess;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transaction.score.field.WriteFieldAccess;
import org.deuce.transaction.score.pool.Pool;
import org.deuce.transaction.score.pool.ResourceFactory;
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
			"Fail on retrieveing an older and unexistent version.");

	protected SCOReReadSet readSet;
	protected SCOReWriteSet writeSet;

	public int sid;
	public String trxID;
	public boolean firstReadDone;
	public List<Integer> votes;
	public int expectedVotes;
	public TimerTask timeoutTask;
	public int retry;

	public SCOReContext()
	{
		super();
		readSet = new SCOReReadSet();
		writeSet = new SCOReWriteSet();
	}
	
	@Override
	protected void initialise(int atomicBlockId, String metainf)
	{
		readSet.clear();
		writeSet.clear();

		trxID = java.util.UUID.randomUUID().toString();
		firstReadDone = false;
		retry++;

		WFAPool.clear();
	}

	@Override
	public DistributedContextState createState()
	{
		return new SCOReContextState(readSet, writeSet, threadID,
				atomicBlockId, sid, trxID);
	}

	/**
	 * Triggers the distributed commit, and waits until it is processed.
	 */
	public boolean commit()
	{
		if (writeSet.isEmpty())
		{ // read-only transaction
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

	/**
	 * @return
	 */
	public Group getInvolvedNodes()
	{
		Group group1 = ((SCOReReadSet) readSet).getInvolvedNodes();
		Group group2 = ((SCOReWriteSet) writeSet).getInvolvedNodes();
		Group resGroup = GroupUtils.unionGroups(group1, group2);
		return resGroup;
	}

	@Override
	protected boolean performValidation()
	{
		return true;
	}

	@Override
	protected void applyUpdates()
	{
		writeSet.apply(sid);
		// CHECKME mais alguma coisa?
	}

	public void recreateContextFromState(DistributedContextState ctxState)
	{
		readSet = (SCOReReadSet) ctxState.rs;
		writeSet = (SCOReWriteSet) ctxState.ws;

		// TODO Auto-generated method stub

		atomicBlockId = -1;

		WFAPool.clear();
	}

	@Override
	public void beforeReadAccess(TxField field)
	{
	}

	private WriteFieldAccess onReadAccess(TxField field)
	{
		ReadFieldAccess current = readSet.getNext();
		current.init((VBoxField) field);

		return writeSet.contains(current);
	}

	@Override
	public ArrayContainer onReadAccess(ArrayContainer value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
		{
			return (ArrayContainer) TribuDSTM.onTxRead(this,
					field.getMetadata(), value);
		}
		else
		{
			return (ArrayContainer) writeAccess.getValue();
		}
	}

	@Override
	public Object onReadAccess(Object value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
		{
			return TribuDSTM.onTxRead(this, field.getMetadata(), value);
		}
		else
		{
			return writeAccess.getValue();
		}
	}

	@Override
	public boolean onReadAccess(boolean value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
		{
			return (Boolean) TribuDSTM.onTxRead(this, field.getMetadata(),
					value);
		}
		else
		{
			return (Boolean) writeAccess.getValue();
		}
	}

	@Override
	public byte onReadAccess(byte value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
		{
			return (Byte) TribuDSTM.onTxRead(this, field.getMetadata(), value);
		}
		else
		{
			return (Byte) writeAccess.getValue();
		}
	}

	@Override
	public char onReadAccess(char value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
		{
			return (Character) TribuDSTM.onTxRead(this, field.getMetadata(),
					value);
		}
		else
		{
			return (Character) writeAccess.getValue();
		}
	}

	@Override
	public short onReadAccess(short value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
		{
			return (Short) TribuDSTM.onTxRead(this, field.getMetadata(), value);
		}
		else
		{
			return (Short) writeAccess.getValue();
		}
	}

	@Override
	public int onReadAccess(int value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
		{
			return (Integer) TribuDSTM.onTxRead(this, field.getMetadata(),
					value);
		}
		else
		{
			return (Integer) writeAccess.getValue();
		}
	}

	@Override
	public long onReadAccess(long value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
		{
			return (Long) TribuDSTM.onTxRead(this, field.getMetadata(), value);
		}
		else
		{
			return (Long) writeAccess.getValue();
		}
	}

	@Override
	public float onReadAccess(float value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
		{
			return (Float) TribuDSTM.onTxRead(this, field.getMetadata(), value);
		}
		else
		{
			return (Float) writeAccess.getValue();
		}
	}

	@Override
	public double onReadAccess(double value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
		{
			return (Double) TribuDSTM
					.onTxRead(this, field.getMetadata(), value);
		}
		else
		{
			return (Double) writeAccess.getValue();
		}
	}

	private void addWriteAccess(WriteFieldAccess write)
	{ // Add to write set
		writeSet.put(write);
	}

	@Override
	public void onWriteAccess(ArrayContainer value, TxField field)
	{ // CHECKME isto pode ser assim?
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(Object value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(boolean value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(byte value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(char value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(short value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(int value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(long value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(float value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(double value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field);
		addWriteAccess(next);
	}

	@Override
	public void onIrrevocableAccess()
	{
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
