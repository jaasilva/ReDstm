package org.deuce.transaction.score;

import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.LocalMetadata;
import org.deuce.distribution.TribuDSTM;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.ReadSet;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.WriteSet;
import org.deuce.transaction.field.*;
import org.deuce.transaction.score.pool.*;
import org.deuce.transaction.tl2.InPlaceLock;
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
@LocalMetadata(metadataClass = "...")
public class SCOReContext extends DistributedContext
{
	public static int MAX_VERSIONS = Integer.getInteger(
			"org.deuce.transaction.score.versions", 16);
	public static final TransactionException VERSION_UNAVAILABLE_EXCEPTION = new TransactionException(
			"Fail on retrieveing an older and unexistent version.");

	/**
	 * Maintains the timestamp that was attributed to the last update
	 * transaction to have committed on this node
	 */
	public static final AtomicInteger commitId = new AtomicInteger(0);
	/**
	 * Keeps track of the next timestamp that this node will propose when it
	 * will receive a commit request for a transaction that accessed some of the
	 * data that it maintains
	 */
	public static final AtomicInteger nextId = new AtomicInteger(0);

	public int sid;

	/**
	 * 
	 */
	public SCOReContext()
	{
		// TODO Auto-generated constructor stub
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.DistributedContext#createReadSet()
	 */
	@Override
	protected ReadSet createReadSet()
	{
		return new SCOReReadSet();
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.DistributedContext#createWriteSet()
	 */
	@Override
	protected WriteSet createWriteSet()
	{
		return new SCOReWriteSet();
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.DistributedContext#createState()
	 */
	@Override
	public DistributedContextState createState()
	{
		return new SCOReContextState(readSet, writeSet, threadID,
				atomicBlockId, sid);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.DistributedContext#initialise(int,
	 * java.lang.String)
	 */
	@Override
	protected void initialise(int atomicBlockId, String metainf)
	{
		// TODO Auto-generated method stub

		arrayPool.clear();
		objectPool.clear();
		booleanPool.clear();
		bytePool.clear();
		charPool.clear();
		shortPool.clear();
		intPool.clear();
		longPool.clear();
		floatPool.clear();
		doublePool.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.DistributedContext#performValidation()
	 */
	@Override
	protected boolean performValidation()
	{
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.DistributedContext#applyUpdates()
	 */
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

		arrayPool.clear();
		objectPool.clear();
		booleanPool.clear();
		bytePool.clear();
		charPool.clear();
		shortPool.clear();
		intPool.clear();
		longPool.clear();
		floatPool.clear();
		doublePool.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.transaction.ContextMetadata#beforeReadAccess(org.deuce.transform
	 * .localmetadata.type.TxField)
	 */
	@Override
	public void beforeReadAccess(TxField field)
	{
		// TODO Auto-generated method stub

	}

	private WriteFieldAccess onReadAccess(TxField field)
	{
		// TODO
		ReadFieldAccess current = readSet.getNext();
		current.init(field);

		return writeSet.contains(current);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.transaction.ContextMetadata#onReadAccess(org.deuce.transform
	 * .localmetadata.array.ArrayContainer,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public ArrayContainer onReadAccess(ArrayContainer value, TxField field)
	{
		return (ArrayContainer) TribuDSTM.onTxRead(this, field.getMetadata());
	}

	public ArrayContainer onLocalReadAccess(ArrayContainer value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		ArrayContainer r = ((ArrayWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(java.lang.Object,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public Object onReadAccess(Object value, TxField field)
	{
		return TribuDSTM.onTxRead(this, field.getMetadata());
	}

	public Object onLocalReadAccess(Object value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		Object r = ((ObjectWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(boolean,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public boolean onReadAccess(boolean value, TxField field)
	{
		return (Boolean) TribuDSTM.onTxRead(this, field.getMetadata());
	}

	public boolean onLocalReadAccess(boolean value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		boolean r = ((BooleanWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(byte,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public byte onReadAccess(byte value, TxField field)
	{
		return (Byte) TribuDSTM.onTxRead(this, field.getMetadata());
	}

	public byte onLocalReadAccess(byte value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		byte r = ((ByteWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(char,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public char onReadAccess(char value, TxField field)
	{
		return (Character) TribuDSTM.onTxRead(this, field.getMetadata());
	}

	public char onLocalReadAccess(char value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		char r = ((CharWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(short,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public short onReadAccess(short value, TxField field)
	{
		return (Short) TribuDSTM.onTxRead(this, field.getMetadata());
	}

	public short onLocalReadAccess(short value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		short r = ((ShortWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(int,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public int onReadAccess(int value, TxField field)
	{
		return (Integer) TribuDSTM.onTxRead(this, field.getMetadata());
	}

	public int onLocalReadAccess(int value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		int r = ((IntWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(long,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public long onReadAccess(long value, TxField field)
	{
		return (Long) TribuDSTM.onTxRead(this, field.getMetadata());
	}

	public long onLocalReadAccess(long value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		long r = ((LongWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(float,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public float onReadAccess(float value, TxField field)
	{
		return (Float) TribuDSTM.onTxRead(this, field.getMetadata());
	}

	public float onLocalReadAccess(float value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		float r = ((FloatWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(double,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public double onReadAccess(double value, TxField field)
	{
		return (Double) TribuDSTM.onTxRead(this, field.getMetadata());
	}

	public double onLocalReadAccess(double value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess(field);
		if (writeAccess == null)
			return value;

		double r = ((DoubleWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	private void addWriteAccess(WriteFieldAccess write)
	{ // Add to write set
		writeSet.put(write);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.transaction.ContextMetadata#onWriteAccess(org.deuce.transform
	 * .localmetadata.array.ArrayContainer,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(ArrayContainer value, TxField field)
	{
		ArrayWriteFieldAccess next = arrayPool.getNext();
		next.set(value, field);
		addWriteAccess(next);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.transaction.ContextMetadata#onWriteAccess(java.lang.Object,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(Object value, TxField field)
	{
		ObjectWriteFieldAccess next = objectPool.getNext();
		next.set(value, field);
		addWriteAccess(next);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(boolean,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(boolean value, TxField field)
	{
		BooleanWriteFieldAccess next = booleanPool.getNext();
		next.set(value, field);
		addWriteAccess(next);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(byte,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(byte value, TxField field)
	{
		ByteWriteFieldAccess next = bytePool.getNext();
		next.set(value, field);
		addWriteAccess(next);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(char,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(char value, TxField field)
	{
		CharWriteFieldAccess next = charPool.getNext();
		next.set(value, field);
		addWriteAccess(next);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(short,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(short value, TxField field)
	{
		ShortWriteFieldAccess next = shortPool.getNext();
		next.set(value, field);
		addWriteAccess(next);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(int,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(int value, TxField field)
	{
		IntWriteFieldAccess next = intPool.getNext();
		next.set(value, field);
		addWriteAccess(next);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(long,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(long value, TxField field)
	{
		LongWriteFieldAccess next = longPool.getNext();
		next.set(value, field);
		addWriteAccess(next);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(float,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(float value, TxField field)
	{
		FloatWriteFieldAccess next = floatPool.getNext();
		next.set(value, field);
		addWriteAccess(next);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(double,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(double value, TxField field)
	{
		DoubleWriteFieldAccess next = doublePool.getNext();
		next.set(value, field);
		addWriteAccess(next);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.IContext#onIrrevocableAccess()
	 */
	@Override
	public void onIrrevocableAccess()
	{
	}

	private static class ArrayResourceFactory implements
			ResourceFactory<ArrayWriteFieldAccess>
	{
		public ArrayWriteFieldAccess newInstance()
		{
			return new ArrayWriteFieldAccess();
		}
	}

	final private Pool<ArrayWriteFieldAccess> arrayPool = new Pool<ArrayWriteFieldAccess>(
			new ArrayResourceFactory());

	private static class ObjectResourceFactory implements
			ResourceFactory<ObjectWriteFieldAccess>
	{
		public ObjectWriteFieldAccess newInstance()
		{
			return new ObjectWriteFieldAccess();
		}
	}

	final private Pool<ObjectWriteFieldAccess> objectPool = new Pool<ObjectWriteFieldAccess>(
			new ObjectResourceFactory());

	private static class BooleanResourceFactory implements
			ResourceFactory<BooleanWriteFieldAccess>
	{
		public BooleanWriteFieldAccess newInstance()
		{
			return new BooleanWriteFieldAccess();
		}
	}

	final private Pool<BooleanWriteFieldAccess> booleanPool = new Pool<BooleanWriteFieldAccess>(
			new BooleanResourceFactory());

	private static class ByteResourceFactory implements
			ResourceFactory<ByteWriteFieldAccess>
	{
		public ByteWriteFieldAccess newInstance()
		{
			return new ByteWriteFieldAccess();
		}
	}

	final private Pool<ByteWriteFieldAccess> bytePool = new Pool<ByteWriteFieldAccess>(
			new ByteResourceFactory());

	private static class CharResourceFactory implements
			ResourceFactory<CharWriteFieldAccess>
	{
		public CharWriteFieldAccess newInstance()
		{
			return new CharWriteFieldAccess();
		}
	}

	final private Pool<CharWriteFieldAccess> charPool = new Pool<CharWriteFieldAccess>(
			new CharResourceFactory());

	private static class ShortResourceFactory implements
			ResourceFactory<ShortWriteFieldAccess>
	{
		public ShortWriteFieldAccess newInstance()
		{
			return new ShortWriteFieldAccess();
		}
	}

	final private Pool<ShortWriteFieldAccess> shortPool = new Pool<ShortWriteFieldAccess>(
			new ShortResourceFactory());

	private static class IntResourceFactory implements
			ResourceFactory<IntWriteFieldAccess>
	{
		public IntWriteFieldAccess newInstance()
		{
			return new IntWriteFieldAccess();
		}
	}

	final private Pool<IntWriteFieldAccess> intPool = new Pool<IntWriteFieldAccess>(
			new IntResourceFactory());

	private static class LongResourceFactory implements
			ResourceFactory<LongWriteFieldAccess>
	{
		public LongWriteFieldAccess newInstance()
		{
			return new LongWriteFieldAccess();
		}
	}

	final private Pool<LongWriteFieldAccess> longPool = new Pool<LongWriteFieldAccess>(
			new LongResourceFactory());

	private static class FloatResourceFactory implements
			ResourceFactory<FloatWriteFieldAccess>
	{
		public FloatWriteFieldAccess newInstance()
		{
			return new FloatWriteFieldAccess();
		}
	}

	final private Pool<FloatWriteFieldAccess> floatPool = new Pool<FloatWriteFieldAccess>(
			new FloatResourceFactory());

	private static class DoubleResourceFactory implements
			ResourceFactory<DoubleWriteFieldAccess>
	{
		public DoubleWriteFieldAccess newInstance()
		{
			return new DoubleWriteFieldAccess();
		}
	}

	final private Pool<DoubleWriteFieldAccess> doublePool = new Pool<DoubleWriteFieldAccess>(
			new DoubleResourceFactory());
}
