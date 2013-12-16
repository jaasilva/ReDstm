package org.deuce.transaction.tl2;

import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.Defaults;
import org.deuce.LocalMetadata;
import org.deuce.distribution.TribuDSTM;
import org.deuce.profiling.PRProfiler;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.tl2.field.ArrayWriteFieldAccess;
import org.deuce.transaction.tl2.field.BooleanWriteFieldAccess;
import org.deuce.transaction.tl2.field.ByteWriteFieldAccess;
import org.deuce.transaction.tl2.field.CharWriteFieldAccess;
import org.deuce.transaction.tl2.field.DoubleWriteFieldAccess;
import org.deuce.transaction.tl2.field.FloatWriteFieldAccess;
import org.deuce.transaction.tl2.field.IntWriteFieldAccess;
import org.deuce.transaction.tl2.field.LongWriteFieldAccess;
import org.deuce.transaction.tl2.field.ObjectWriteFieldAccess;
import org.deuce.transaction.tl2.field.ReadFieldAccess;
import org.deuce.transaction.tl2.field.ShortWriteFieldAccess;
import org.deuce.transaction.tl2.field.WriteFieldAccess;
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
	private static final boolean TX_LOAD_OPT = Boolean
			.getBoolean(Defaults.TL2_TX_LOAD_OPT);

	protected ReadSet readSet;
	protected WriteSet writeSet;

	private ReadFieldAccess currentReadFieldAccess = null;

	final static AtomicInteger clock = new AtomicInteger(0);
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

		readSet = new ReadSet();
		writeSet = new WriteSet();

		localClock = clock.get();
	}

	public void recreateContextFromState(DistributedContextState ctxState)
	{
		super.recreateContextFromState(ctxState);

		readSet = (ReadSet) ctxState.rs;
		writeSet = (WriteSet) ctxState.ws;

		localClock = ((ContextState) ctxState).rv;

		atomicBlockId = -1; // Remote tx

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

		localClock = clock.get();

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
	}

	protected boolean performValidation()
	{
		try
		{ // pre commit validation phase
			writeSet.forEach(lockProcedure);
			readSet.checkClock(localClock, this);
		}
		catch (TransactionException exception)
		{ // Abort tx. Release locks
			writeSet.forEach(lockProcedure.unlockProcedure);
			return false;
		}

		return true;
	}

	protected void applyUpdates()
	{ // commit new values and release locks
		writeSet.forEach(putProcedure);

		lockProcedure.setAndUnlockProcedure.retrieveNewClock(); // clock++
		writeSet.forEach(lockProcedure.setAndUnlockProcedure);

		lockProcedure.clear();
	}

	private WriteFieldAccess onReadAccess0(TxField field)
	{
		ReadFieldAccess current = null;

		if (!TX_LOAD_OPT)
		{
			current = currentReadFieldAccess;

			// Check the read is still valid
			((InPlaceLock) field).checkLock(localClock, lastReadLock);
		}
		else
		{
			current = readSet.getNext();
			current.init(field);

			((InPlaceLock) field).checkLock2(localClock);
		}

		// Check if it is already included in the write set
		return writeSet.contains(current);
	}

	private void addWriteAccess0(WriteFieldAccess write)
	{ // Add to write set
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

	/***************************************
	 * ON READ ACCESS
	 **************************************/

	public ArrayContainer onReadAccess(ArrayContainer value, TxField field)
	{
		PRProfiler.onTxLocalReadBegin(threadID);
		ArrayContainer res = null;
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			res = value;
		}
		else
		{
			res = ((ArrayWriteFieldAccess) writeAccess).getValue();
		}
		PRProfiler.onTxLocalReadFinish(threadID);
		return res;
	}

	public Object onReadAccess(Object value, TxField field)
	{
		PRProfiler.onTxLocalReadBegin(threadID);
		Object res = null;
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			res = value;
		}
		else
		{
			res = ((ObjectWriteFieldAccess) writeAccess).getValue();
		}
		PRProfiler.onTxLocalReadFinish(threadID);
		return res;
	}

	public boolean onReadAccess(boolean value, TxField field)
	{
		PRProfiler.onTxLocalReadBegin(threadID);
		boolean res = false;
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			res = value;
		}
		else
		{
			res = ((BooleanWriteFieldAccess) writeAccess).getValue();
		}
		PRProfiler.onTxLocalReadFinish(threadID);
		return res;
	}

	public byte onReadAccess(byte value, TxField field)
	{
		PRProfiler.onTxLocalReadBegin(threadID);
		byte res = -1;
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			res = value;
		}
		else
		{
			res = ((ByteWriteFieldAccess) writeAccess).getValue();
		}
		PRProfiler.onTxLocalReadFinish(threadID);
		return res;
	}

	public char onReadAccess(char value, TxField field)
	{
		PRProfiler.onTxLocalReadBegin(threadID);
		char res = ' ';
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			res = value;
		}
		else
		{
			res = ((CharWriteFieldAccess) writeAccess).getValue();
		}
		PRProfiler.onTxLocalReadFinish(threadID);
		return res;
	}

	public short onReadAccess(short value, TxField field)
	{
		PRProfiler.onTxLocalReadBegin(threadID);
		short res = -1;
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			res = value;
		}
		else
		{
			res = ((ShortWriteFieldAccess) writeAccess).getValue();
		}
		PRProfiler.onTxLocalReadFinish(threadID);
		return res;

	}

	public int onReadAccess(int value, TxField field)
	{
		PRProfiler.onTxLocalReadBegin(threadID);
		int res = -1;
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			res = value;
		}
		else
		{
			res = ((IntWriteFieldAccess) writeAccess).getValue();
		}
		PRProfiler.onTxLocalReadFinish(threadID);
		return res;
	}

	public long onReadAccess(long value, TxField field)
	{
		PRProfiler.onTxLocalReadBegin(threadID);
		long res = -1;
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			res = value;
		}
		else
		{
			res = ((LongWriteFieldAccess) writeAccess).getValue();
		}
		PRProfiler.onTxLocalReadFinish(threadID);
		return res;
	}

	public float onReadAccess(float value, TxField field)
	{
		PRProfiler.onTxLocalReadBegin(threadID);
		float res = -1;
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			res = value;
		}
		else
		{
			res = ((FloatWriteFieldAccess) writeAccess).getValue();
		}
		PRProfiler.onTxLocalReadFinish(threadID);
		return res;
	}

	public double onReadAccess(double value, TxField field)
	{
		PRProfiler.onTxLocalReadBegin(threadID);
		double res = -1;
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			res = value;
		}
		else
		{
			res = ((DoubleWriteFieldAccess) writeAccess).getValue();
		}
		PRProfiler.onTxLocalReadFinish(threadID);
		return res;
	}

	/***************************************
	 * ON WRITE ACCESS
	 **************************************/

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
		PRProfiler.onTxAppFinish(threadID);

		if (writeSet.isEmpty())
		{
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
