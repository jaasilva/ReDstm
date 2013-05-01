package org.deuce.transaction.tl2;

import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.LocalMetadata;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.ReadSet;
import org.deuce.transaction.SpeculativeContextState;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.WriteSet;
import org.deuce.transaction.field.ReadFieldAccess;
import org.deuce.transaction.field.SpeculativeArrayWriteFieldAccess;
import org.deuce.transaction.field.SpeculativeBooleanWriteFieldAccess;
import org.deuce.transaction.field.SpeculativeByteWriteFieldAccess;
import org.deuce.transaction.field.SpeculativeCharWriteFieldAccess;
import org.deuce.transaction.field.SpeculativeDoubleWriteFieldAccess;
import org.deuce.transaction.field.SpeculativeFloatWriteFieldAccess;
import org.deuce.transaction.field.SpeculativeIntWriteFieldAccess;
import org.deuce.transaction.field.SpeculativeLongWriteFieldAccess;
import org.deuce.transaction.field.SpeculativeObjectWriteFieldAccess;
import org.deuce.transaction.field.SpeculativeShortWriteFieldAccess;
import org.deuce.transaction.field.SpeculativeWriteFieldAccess;
import org.deuce.transaction.field.WriteFieldAccess;
import org.deuce.transaction.tl2.pool.Pool;
import org.deuce.transaction.tl2.pool.ResourceFactory;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ArrayContainer;
import org.deuce.transform.localmetadata.type.TxField;
import org.deuce.transform.localmetadata.type.speculative.SpeculativeTxField;
import org.deuce.trove.TObjectProcedure;

/**
 * Distributed TL2 w/ speculation support.
 * 
 * @author Tiago Vale
 */
@ExcludeTM
@LocalMetadata(metadataClass = "org.deuce.transaction.tl2.SpeculativeTL2Field")
final public class SpeculativeContext extends
		org.deuce.transaction.SpeculativeContext
{
	public static final TransactionException RO_WRITE = new TransactionException(
			"Read-only transaction tried to write");
	private static final boolean TX_LOAD_OPT = Boolean
			.getBoolean("org.deuce.transaction.distributed.tl2.txload.opt");

	final static public AtomicInteger clock = new AtomicInteger(0);
	/*
	 * FIXMEs: Refactor. Speculative clock.
	 */
	final static public AtomicInteger speculativeClock = new AtomicInteger(0);

	private ReadFieldAccess currentReadFieldAccess = null;

	// Marked on beforeRead, used for the double lock check
	public int localClock;
	private int lastReadLock;

	final private SpeculativeLockProcedure lockProcedure = new SpeculativeLockProcedure(
			this);

	final private TObjectProcedure<WriteFieldAccess> putProcedure = new TObjectProcedure<WriteFieldAccess>()
	{

		public boolean execute(WriteFieldAccess writeField)
		{
			writeField.put();
			return true;
		}

	};
	/*
	 * FIXMEs: Refactor. Speculative put procedure.
	 */
	final private TObjectProcedure<WriteFieldAccess> speculativePutProcedure = new TObjectProcedure<WriteFieldAccess>()
	{
		public boolean execute(WriteFieldAccess writeField)
		{
			((SpeculativeWriteFieldAccess) writeField)
					.speculativePut(SpeculativeContext.this);
			return true;
		}
	};

	protected int lastAtomicBlockId = -1;
	protected boolean readOnly = true;

	public SpeculativeContext()
	{
		super();
		localClock = speculativeClock.get();
	}

	public void recreateContextFromState(DistributedContextState ctxState)
	{
		super.recreateContextFromState(ctxState);
		localClock = ((SpeculativeContextState) ctxState).speculativeVersionNumber;
		readOnly = false;

		// boolean done = false;
		// do {
		// int c = speculativeClock.get();
		// if (localClock > c)
		// done = speculativeClock.compareAndSet(c, localClock);
		// else
		// done = true;
		// } while (!done);
	}

	protected ReadSet createReadSet()
	{
		return new SpeculativeTL2ReadSet();
	}

	protected WriteSet createWriteSet()
	{
		return new WriteSet();
	}

	public DistributedContextState createState()
	{
		return new SpeculativeContextState(readSet, writeSet, threadID,
				atomicBlockId, localClock);
	}

	public void initialise(int atomicBlockId, String metainf)
	{
		this.currentReadFieldAccess = null;
		this.localClock = speculativeClock.get();
		this.arrayPool.clear();
		this.objectPool.clear();
		this.booleanPool.clear();
		this.bytePool.clear();
		this.charPool.clear();
		this.shortPool.clear();
		this.intPool.clear();
		this.longPool.clear();
		this.floatPool.clear();
		this.doublePool.clear();
		this.aborted = false;
		if (lastAtomicBlockId != atomicBlockId)
		{
			readOnly = true;
			lastAtomicBlockId = atomicBlockId;
		}
	}

	public boolean performValidation()
	{
		boolean valid = true;
		try
		{
			((SpeculativeTL2ReadSet) readSet).checkClock(localClock);
		}
		catch (TransactionException exception)
		{
			valid = false;
		}

		return valid;
	}

	protected boolean performSpeculativeValidation()
	{
		boolean valid = true;
		try
		{
			((SpeculativeTL2ReadSet) readSet).speculativeCheckClock(localClock);
		}
		catch (TransactionException exception)
		{
			valid = false;
		}

		return valid;
	}

	public void applyUpdates()
	{
		writeSet.forEach(lockProcedure);

		// commit new values and release locks
		writeSet.forEach(putProcedure);

		lockProcedure.setAndUnlockProcedure.retrieveNewClock();
		writeSet.forEach(lockProcedure.setAndUnlockProcedure);

		lockProcedure.clear();
	}

	protected void applySpeculativeUpdates()
	{
		writeSet.forEach(lockProcedure);

		writeSet.forEach(speculativePutProcedure);

		localClock = speculativeClock.incrementAndGet();

		writeSet.forEach(lockProcedure.unlockProcedure);

		// lockProcedure.clear();
	}

	public void performSpeculativeAbort()
	{
		writeSet.forEach(lockProcedure);

		writeSet.forEach(new TObjectProcedure<WriteFieldAccess>()
		{
			public boolean execute(WriteFieldAccess writeField)
			{
				((SpeculativeWriteFieldAccess) writeField).speculativeRemove();
				return true;
			}
		});

		writeSet.forEach(lockProcedure.unlockProcedure);
	}

	public int getSpeculativeVersionNumber()
	{
		return localClock;
	}

	public void resetSpeculativeVersionNumbers()
	{
		speculativeClock.set(clock.get());
	}

	private WriteFieldAccess onReadAccess0(TxField field)
	{
		if (!TX_LOAD_OPT)
		{
			ReadFieldAccess current = currentReadFieldAccess;

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
		if (readOnly)
		{
			readOnly = false;
			throw RO_WRITE;
		}
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
		{
			try
			{
				if (readOnly)
					return value;
				else
					return ((SpeculativeTxField) field)
							.speculativeReadArray(this);
			}
			finally
			{
				((InPlaceLock) field).checkLock(localClock, lastReadLock);
			}
		}

		ArrayContainer r = ((SpeculativeArrayWriteFieldAccess) writeAccess)
				.getValue();

		return r;
	}

	public Object onReadAccess(Object value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			try
			{
				if (readOnly)
					return value;
				else
					return ((SpeculativeTxField) field)
							.speculativeReadObject(this);
			}
			finally
			{
				((InPlaceLock) field).checkLock(localClock, lastReadLock);
			}
		}

		Object r = ((SpeculativeObjectWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public boolean onReadAccess(boolean value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			try
			{
				if (readOnly)
					return value;
				else
					return ((SpeculativeTxField) field)
							.speculativeReadBoolean(this);
			}
			finally
			{
				((InPlaceLock) field).checkLock(localClock, lastReadLock);
			}
		}

		boolean r = ((SpeculativeBooleanWriteFieldAccess) writeAccess)
				.getValue();

		return r;
	}

	public byte onReadAccess(byte value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			try
			{
				if (readOnly)
					return value;
				else
					return ((SpeculativeTxField) field)
							.speculativeReadByte(this);
			}
			finally
			{
				((InPlaceLock) field).checkLock(localClock, lastReadLock);
			}
		}

		byte r = ((SpeculativeByteWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public char onReadAccess(char value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			try
			{
				if (readOnly)
					return value;
				else
					return ((SpeculativeTxField) field)
							.speculativeReadChar(this);
			}
			finally
			{
				((InPlaceLock) field).checkLock(localClock, lastReadLock);
			}
		}

		char r = ((SpeculativeCharWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public short onReadAccess(short value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			try
			{
				if (readOnly)
					return value;
				else
					return ((SpeculativeTxField) field)
							.speculativeReadShort(this);
			}
			finally
			{
				((InPlaceLock) field).checkLock(localClock, lastReadLock);
			}
		}

		short r = ((SpeculativeShortWriteFieldAccess) writeAccess).getValue();

		return r;

	}

	public int onReadAccess(int value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			try
			{
				if (readOnly)
					return value;
				else
					return ((SpeculativeTxField) field)
							.speculativeReadInt(this);
			}
			finally
			{
				((InPlaceLock) field).checkLock(localClock, lastReadLock);
			}
		}

		int r = ((SpeculativeIntWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public long onReadAccess(long value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			try
			{
				if (readOnly)
					return value;
				else
					return ((SpeculativeTxField) field)
							.speculativeReadLong(this);
			}
			finally
			{
				((InPlaceLock) field).checkLock(localClock, lastReadLock);
			}
		}

		long r = ((SpeculativeLongWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public float onReadAccess(float value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			try
			{
				if (readOnly)
					return value;
				else
					return ((SpeculativeTxField) field)
							.speculativeReadFloat(this);
			}
			finally
			{
				((InPlaceLock) field).checkLock(localClock, lastReadLock);
			}
		}

		float r = ((SpeculativeFloatWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public double onReadAccess(double value, TxField field)
	{
		WriteFieldAccess writeAccess = onReadAccess0(field);
		if (writeAccess == null)
		{
			try
			{
				if (readOnly)
					return value;
				else
					return ((SpeculativeTxField) field)
							.speculativeReadDouble(this);
			}
			finally
			{
				((InPlaceLock) field).checkLock(localClock, lastReadLock);
			}
		}

		double r = ((SpeculativeDoubleWriteFieldAccess) writeAccess).getValue();

		return r;
	}

	public void onWriteAccess(ArrayContainer value, TxField field)
	{

		SpeculativeArrayWriteFieldAccess next = arrayPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(Object value, TxField field)
	{

		SpeculativeObjectWriteFieldAccess next = objectPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(boolean value, TxField field)
	{

		SpeculativeBooleanWriteFieldAccess next = booleanPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(byte value, TxField field)
	{

		SpeculativeByteWriteFieldAccess next = bytePool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(char value, TxField field)
	{

		SpeculativeCharWriteFieldAccess next = charPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(short value, TxField field)
	{

		SpeculativeShortWriteFieldAccess next = shortPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(int value, TxField field)
	{

		SpeculativeIntWriteFieldAccess next = intPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(long value, TxField field)
	{

		SpeculativeLongWriteFieldAccess next = longPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(float value, TxField field)
	{

		SpeculativeFloatWriteFieldAccess next = floatPool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	public void onWriteAccess(double value, TxField field)
	{

		SpeculativeDoubleWriteFieldAccess next = doublePool.getNext();
		next.set(value, field);
		addWriteAccess0(next);

	}

	private static class ArrayResourceFactory implements
			ResourceFactory<SpeculativeArrayWriteFieldAccess>
	{
		public SpeculativeArrayWriteFieldAccess newInstance()
		{
			return new SpeculativeArrayWriteFieldAccess();
		}
	}

	final private Pool<SpeculativeArrayWriteFieldAccess> arrayPool = new Pool<SpeculativeArrayWriteFieldAccess>(
			new ArrayResourceFactory());

	private static class ObjectResourceFactory implements
			ResourceFactory<SpeculativeObjectWriteFieldAccess>
	{
		public SpeculativeObjectWriteFieldAccess newInstance()
		{
			return new SpeculativeObjectWriteFieldAccess();
		}
	}

	final private Pool<SpeculativeObjectWriteFieldAccess> objectPool = new Pool<SpeculativeObjectWriteFieldAccess>(
			new ObjectResourceFactory());

	private static class BooleanResourceFactory implements
			ResourceFactory<SpeculativeBooleanWriteFieldAccess>
	{
		public SpeculativeBooleanWriteFieldAccess newInstance()
		{
			return new SpeculativeBooleanWriteFieldAccess();
		}
	}

	final private Pool<SpeculativeBooleanWriteFieldAccess> booleanPool = new Pool<SpeculativeBooleanWriteFieldAccess>(
			new BooleanResourceFactory());

	private static class ByteResourceFactory implements
			ResourceFactory<SpeculativeByteWriteFieldAccess>
	{
		public SpeculativeByteWriteFieldAccess newInstance()
		{
			return new SpeculativeByteWriteFieldAccess();
		}
	}

	final private Pool<SpeculativeByteWriteFieldAccess> bytePool = new Pool<SpeculativeByteWriteFieldAccess>(
			new ByteResourceFactory());

	private static class CharResourceFactory implements
			ResourceFactory<SpeculativeCharWriteFieldAccess>
	{
		public SpeculativeCharWriteFieldAccess newInstance()
		{
			return new SpeculativeCharWriteFieldAccess();
		}
	}

	final private Pool<SpeculativeCharWriteFieldAccess> charPool = new Pool<SpeculativeCharWriteFieldAccess>(
			new CharResourceFactory());

	private static class ShortResourceFactory implements
			ResourceFactory<SpeculativeShortWriteFieldAccess>
	{
		public SpeculativeShortWriteFieldAccess newInstance()
		{
			return new SpeculativeShortWriteFieldAccess();
		}
	}

	final private Pool<SpeculativeShortWriteFieldAccess> shortPool = new Pool<SpeculativeShortWriteFieldAccess>(
			new ShortResourceFactory());

	private static class IntResourceFactory implements
			ResourceFactory<SpeculativeIntWriteFieldAccess>
	{
		public SpeculativeIntWriteFieldAccess newInstance()
		{
			return new SpeculativeIntWriteFieldAccess();
		}
	}

	final private Pool<SpeculativeIntWriteFieldAccess> intPool = new Pool<SpeculativeIntWriteFieldAccess>(
			new IntResourceFactory());

	private static class LongResourceFactory implements
			ResourceFactory<SpeculativeLongWriteFieldAccess>
	{
		public SpeculativeLongWriteFieldAccess newInstance()
		{
			return new SpeculativeLongWriteFieldAccess();
		}
	}

	final private Pool<SpeculativeLongWriteFieldAccess> longPool = new Pool<SpeculativeLongWriteFieldAccess>(
			new LongResourceFactory());

	private static class FloatResourceFactory implements
			ResourceFactory<SpeculativeFloatWriteFieldAccess>
	{
		public SpeculativeFloatWriteFieldAccess newInstance()
		{
			return new SpeculativeFloatWriteFieldAccess();
		}
	}

	final private Pool<SpeculativeFloatWriteFieldAccess> floatPool = new Pool<SpeculativeFloatWriteFieldAccess>(
			new FloatResourceFactory());

	private static class DoubleResourceFactory implements
			ResourceFactory<SpeculativeDoubleWriteFieldAccess>
	{
		public SpeculativeDoubleWriteFieldAccess newInstance()
		{
			return new SpeculativeDoubleWriteFieldAccess();
		}
	}

	final private Pool<SpeculativeDoubleWriteFieldAccess> doublePool = new Pool<SpeculativeDoubleWriteFieldAccess>(
			new DoubleResourceFactory());

	@Override
	public void onIrrevocableAccess()
	{

	}
}
