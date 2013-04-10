package org.deuce.transaction.score;

import org.deuce.LocalMetadata;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.GroupUtils;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.ReadSet;
import org.deuce.transaction.WriteSet;
import org.deuce.transaction.score.field.ReadFieldAccess;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transaction.score.field.VBoxField.__Type;
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
	private SCOReReadSet readSet = (SCOReReadSet) super.readSet;
	private SCOReWriteSet writeSet = (SCOReWriteSet) super.writeSet;
	
	/**
	 * Snapshot id for the transactions of this context
	 */
	public int sid;
	/**
	 * Transaction id that uniquely identifies a transaction in the entire
	 * system
	 */
	public String trxID;
	/**
	 * 
	 */
	public boolean firstReadDone;

	public SCOReContext()
	{
		super();
		trxID = java.util.UUID.randomUUID().toString();
		firstReadDone = false;
	}

	@Override
	protected ReadSet createReadSet()
	{
		return new SCOReReadSet();
	}

	@Override
	protected WriteSet createWriteSet()
	{
		return new SCOReWriteSet();
	}

	@Override
	public DistributedContextState createState()
	{
		return new SCOReContextState(readSet, writeSet, threadID,
				atomicBlockId, sid, trxID);
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
	protected void initialise(int atomicBlockId, String metainf)
	{
		// TODO Auto-generated method stub
		trxID = java.util.UUID.randomUUID().toString();
		firstReadDone = false;

		WFAPool.clear();
	}

	@Override
	protected boolean performValidation()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void applyUpdates()
	{
		// TODO Auto-generated method stub

	}

	public void recreateContextFromState(DistributedContextState ctxState)
	{
		super.recreateContextFromState(ctxState);

		// TODO Auto-generated method stub

		atomicBlockId = -1;

		WFAPool.clear();
	}

	@Override
	public void beforeReadAccess(TxField field)
	{
		// TODO Auto-generated method stub
	}

	private WriteFieldAccess onReadAccess(TxField field)
	{
		// TODO
		ReadFieldAccess current = readSet.scoreGetNext();
		current.init((VBoxField) field);

		return writeSet.contains(current);
	}

	@Override
	public ArrayContainer onReadAccess(ArrayContainer value, TxField field)
	{
		return (ArrayContainer) TribuDSTM.onTxRead(this, field.getMetadata(),
				value);
	}

	public ArrayContainer onLocalReadAccess(ArrayContainer value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		ArrayContainer r = (ArrayContainer) writeAccess.getValue();

		return r;
	}

	@Override
	public Object onReadAccess(Object value, TxField field)
	{
		return TribuDSTM.onTxRead(this, field.getMetadata(), value);
	}

	public Object onLocalReadAccess(Object value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		Object r = (Object) writeAccess.getValue();

		return r;
	}

	@Override
	public boolean onReadAccess(boolean value, TxField field)
	{
		return (Boolean) TribuDSTM.onTxRead(this, field.getMetadata(), value);
	}

	public boolean onLocalReadAccess(boolean value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		boolean r = (Boolean) writeAccess.getValue();

		return r;
	}

	@Override
	public byte onReadAccess(byte value, TxField field)
	{
		return (Byte) TribuDSTM.onTxRead(this, field.getMetadata(), value);
	}

	public byte onLocalReadAccess(byte value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		byte r = (Byte) writeAccess.getValue();

		return r;
	}

	@Override
	public char onReadAccess(char value, TxField field)
	{
		return (Character) TribuDSTM.onTxRead(this, field.getMetadata(), value);
	}

	public char onLocalReadAccess(char value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		char r = (Character) writeAccess.getValue();

		return r;
	}

	@Override
	public short onReadAccess(short value, TxField field)
	{
		return (Short) TribuDSTM.onTxRead(this, field.getMetadata(), value);
	}

	public short onLocalReadAccess(short value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		short r = (Short) writeAccess.getValue();

		return r;
	}

	@Override
	public int onReadAccess(int value, TxField field)
	{
		return (Integer) TribuDSTM.onTxRead(this, field.getMetadata(), value);
	}

	public int onLocalReadAccess(int value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		int r = (Integer) writeAccess.getValue();

		return r;
	}

	@Override
	public long onReadAccess(long value, TxField field)
	{
		return (Long) TribuDSTM.onTxRead(this, field.getMetadata(), value);
	}

	public long onLocalReadAccess(long value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		long r = (Long) writeAccess.getValue();

		return r;
	}

	@Override
	public float onReadAccess(float value, TxField field)
	{
		return (Float) TribuDSTM.onTxRead(this, field.getMetadata(), value);
	}

	public float onLocalReadAccess(float value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		float r = (Float) writeAccess.getValue();

		return r;
	}

	@Override
	public double onReadAccess(double value, TxField field)
	{
		return (Double) TribuDSTM.onTxRead(this, field.getMetadata(), value);
	}

	public double onLocalReadAccess(double value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		double r =  (Double) writeAccess.getValue();

		return r;
	}

	private void addWriteAccess(WriteFieldAccess write)
	{ // Add to write set
		writeSet.put(write);
	}

	@Override
	public void onWriteAccess(ArrayContainer value, TxField field)
	{ // CHECKME isto pode ser assim?
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field, __Type.OBJECT);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(Object value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field, __Type.OBJECT);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(boolean value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field, __Type.BOOLEAN);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(byte value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field, __Type.BYTE);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(char value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field, __Type.CHAR);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(short value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field, __Type.SHORT);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(int value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field, __Type.INT);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(long value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field, __Type.LONG);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(float value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field, __Type.FLOAT);
		addWriteAccess(next);
	}

	@Override
	public void onWriteAccess(double value, TxField field)
	{
		WriteFieldAccess next = WFAPool.getNext();
		next.set(value, (VBoxField) field, __Type.DOUBLE);
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
