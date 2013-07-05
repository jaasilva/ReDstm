package org.deuce.transaction.tl2;

import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.LocalMetadata;
import org.deuce.distribution.TribuDSTM;
import org.deuce.profiling.PRProfiler;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.ReadSet;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.WriteSet;
import org.deuce.transaction.field.ArrayWriteFieldAccess;
import org.deuce.transaction.field.BooleanWriteFieldAccess;
import org.deuce.transaction.field.ByteWriteFieldAccess;
import org.deuce.transaction.field.CharWriteFieldAccess;
import org.deuce.transaction.field.DoubleWriteFieldAccess;
import org.deuce.transaction.field.FloatWriteFieldAccess;
import org.deuce.transaction.field.IntWriteFieldAccess;
import org.deuce.transaction.field.LongWriteFieldAccess;
import org.deuce.transaction.field.ObjectWriteFieldAccess;
import org.deuce.transaction.field.ReadFieldAccess;
import org.deuce.transaction.field.ShortWriteFieldAccess;
import org.deuce.transaction.field.WriteFieldAccess;
import org.deuce.transaction.pool.Pool;
import org.deuce.transaction.pool.ResourceFactory;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ArrayContainer;
import org.deuce.transform.localmetadata.type.TxField;
import org.deuce.trove.TObjectProcedure;

/**
 * Distributed TL2.
 * 
 * @author Tiago Vale
 * @author Ricardo Dias
 */
@ExcludeTM
@LocalMetadata(metadataClass = "org.deuce.transaction.tl2.TL2Field")
public class Context extends DistributedContext
{

	/**
	 * The transaction's read set.
	 */
	protected ReadSet readSet;

	/**
	 * The transaction's write set.
	 */
	protected WriteSet writeSet;

	private static final boolean TX_LOAD_OPT = Boolean
			.getBoolean("org.deuce.transaction.tl2.txload.opt");

	final static AtomicInteger clock = new AtomicInteger(0);

	private ReadFieldAccess currentReadFieldAccess = null;

	// Marked on beforeRead, used for the double lock check
	private int localClock;
	private int lastReadLock;

	final private LockProcedure lockProcedure = new LockProcedure(this);

	final private TObjectProcedure<WriteFieldAccess> putProcedure = new TObjectProcedure<WriteFieldAccess>()
	{

		public boolean execute(WriteFieldAccess writeField)
		{
			writeField.put();
			return true;
		}

	};

	public Context()
	{
		super();

		readSet = new TL2ReadSet();
		writeSet = new WriteSet();

		localClock = clock.get();
	}

	public void recreateContextFromState(DistributedContextState ctxState)
	{
		super.recreateContextFromState(ctxState);

		readSet = (ReadSet) ctxState.rs;
		writeSet = (WriteSet) ctxState.ws;

		localClock = ((ContextState) ctxState).rv;

		atomicBlockId = -1;

		currentReadFieldAccess = null;
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

		// boolean done = false;
		// do {
		// int c = clock.get();
		// if (localClock > c)
		// done = clock.compareAndSet(c, localClock);
		// else
		// done = true;
		// } while (!done);
	}

	public DistributedContextState createState()
	{
		return new ContextState(readSet, writeSet, threadID, atomicBlockId,
				localClock);
	}

	public void initialise(int atomicBlockId, String metainf)
	{
		readSet.clear();
		writeSet.clear();

		currentReadFieldAccess = null;
		localClock = clock.get();
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

	protected boolean performValidation()
	{
		try
		{
			// pre commit validation phase
			writeSet.forEach(lockProcedure);
			((TL2ReadSet) readSet).checkClock(localClock, this);
		}
		catch (TransactionException exception)
		{
			writeSet.forEach(lockProcedure.unlockProcedure);
			return false;
		}

		return true;
	}

	protected void applyUpdates()
	{
		// commit new values and release locks
		writeSet.forEach(putProcedure);

		lockProcedure.setAndUnlockProcedure.retrieveNewClock();
		writeSet.forEach(lockProcedure.setAndUnlockProcedure);

		lockProcedure.clear();
	}

	private WriteFieldAccess onReadAccess0(TxField field)
	{
		if (!TX_LOAD_OPT)
		{
			ReadFieldAccess current = currentReadFieldAccess;

			// Check the read is still valid
			((InPlaceLock) field).checkLock(localClock, lastReadLock);

			// Check if it is already included in the write set
			return writeSet.contains(current);
		}
		else
		{
			ReadFieldAccess current = readSet.getNext();
			current.init(field);

			((InPlaceLock) field).checkLock2(localClock);

			return writeSet.contains(current);
		}
	}

	private void addWriteAccess0(WriteFieldAccess write)
	{
		// Add to write set
		writeSet.put(write);
	}

	public void beforeReadAccess(TxField field)
	{
		if (!TX_LOAD_OPT)
		{
			ReadFieldAccess next = readSet.getNext();
			currentReadFieldAccess = next;
			next.init(field);

			// Check the read is still valid
			lastReadLock = ((InPlaceLock) field).checkLock(localClock);
		}
	}

	public ArrayContainer onReadAccess(ArrayContainer value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
			return value;

		ArrayContainer r = ((ArrayWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public Object onReadAccess(Object value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
			return value;

		Object r = ((ObjectWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public boolean onReadAccess(boolean value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
			return value;

		boolean r = ((BooleanWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public byte onReadAccess(byte value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
			return value;

		byte r = ((ByteWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public char onReadAccess(char value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
			return value;

		char r = ((CharWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public short onReadAccess(short value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
			return value;

		short r = ((ShortWriteFieldAccess) writeAccess).getValue();

		return r;

	}

	public int onReadAccess(int value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
			return value;

		int r = ((IntWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public long onReadAccess(long value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
			return value;

		long r = ((LongWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public float onReadAccess(float value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
			return value;

		float r = ((FloatWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public double onReadAccess(double value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
			return value;

		double r = ((DoubleWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public void onWriteAccess(ArrayContainer value, TxField field)
	{

		ArrayWriteFieldAccess next = arrayPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(Object value, TxField field)
	{

		ObjectWriteFieldAccess next = objectPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(boolean value, TxField field)
	{

		BooleanWriteFieldAccess next = booleanPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(byte value, TxField field)
	{

		ByteWriteFieldAccess next = bytePool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(char value, TxField field)
	{

		CharWriteFieldAccess next = charPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(short value, TxField field)
	{

		ShortWriteFieldAccess next = shortPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(int value, TxField field)
	{

		IntWriteFieldAccess next = intPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(long value, TxField field)
	{

		LongWriteFieldAccess next = longPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(float value, TxField field)
	{

		FloatWriteFieldAccess next = floatPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(double value, TxField field)
	{

		DoubleWriteFieldAccess next = doublePool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

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

	@Override
	public void onIrrevocableAccess()
	{
	}

	/**
	 * Triggers the distributed commit, and waits until it is processed.
	 */
	public boolean commit()
	{
		// profiler.onTxAppCommit();
		PRProfiler.onTxAppFinish(threadID);

		if (writeSet.isEmpty())
		{

			// if (Profiler.enabled)
			// profiler.txCommitted++;
			PRProfiler.txProcessed(true);

			TribuDSTM.onTxFinished(this, true);
			return true;
		}

		TribuDSTM.onTxCommit(this);
		try
		{
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
}
