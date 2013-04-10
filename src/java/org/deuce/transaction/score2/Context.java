package org.deuce.transaction.score2;

import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.LocalMetadata;
import org.deuce.transaction.ContextMetadata;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.score2.field.*;
import org.deuce.transaction.score.pool.*;
import org.deuce.transaction.util.BooleanArrayList;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ArrayContainer;
import org.deuce.transform.localmetadata.type.TxField;
import org.deuce.trove.TObjectProcedure;

/**
 * Versioned STM with bounded number of versions based in the JVSTM and TL2
 * algorithm. This version uses lock per write-set entry during commit, as in
 * TL2, opposed to the global lock in the original JVSTM algorithm.
 * 
 * 
 * @author Ricardo Dias
 * @version Oct 11, 2010 15:00:19 PM
 * 
 */

// @InPlaceMetadata(
// fieldObjectClass="org.deuce.transaction.mvstm.field.VBoxFieldO",
// fieldIntClass="org.deuce.transaction.mvstm.field.VBoxFieldI",
// fieldShortClass="org.deuce.transaction.mvstm.field.VBoxFieldS",
// fieldCharClass="org.deuce.transaction.mvstm.field.VBoxFieldC",
// fieldByteClass="org.deuce.transaction.mvstm.field.VBoxFieldB",
// fieldBooleanClass="org.deuce.transaction.mvstm.field.VBoxFieldZ",
// fieldFloatClass="org.deuce.transaction.mvstm.field.VBoxFieldF",
// fieldLongClass="org.deuce.transaction.mvstm.field.VBoxFieldL",
// fieldDoubleClass="org.deuce.transaction.mvstm.field.VBoxFieldD",
//
// arrayObjectClass="org.deuce.transaction.mvstm.field.VBoxArrO",
// arrayIntClass="org.deuce.transaction.mvstm.field.VBoxArrI",
// arrayShortClass="org.deuce.transaction.mvstm.field.VBoxArrS",
// arrayCharClass="org.deuce.transaction.mvstm.field.VBoxArrC",
// arrayByteClass="org.deuce.transaction.mvstm.field.VBoxArrB",
// arrayBooleanClass="org.deuce.transaction.mvstm.field.VBoxArrZ",
// arrayFloatClass="org.deuce.transaction.mvstm.field.VBoxArrF",
// arrayLongClass="org.deuce.transaction.mvstm.field.VBoxArrL",
// arrayDoubleClass="org.deuce.transaction.mvstm.field.VBoxArrD"
// )

@ExcludeTM
@LocalMetadata(metadataClass = "org.deuce.transform.localmetadata.type.TxField")
public final class Context implements ContextMetadata
{
	private static final TransactionException READ_ONLY_FAILURE_EXCEPTION = new TransactionException(
			"Fail on write (read-only hint was set).");

	public static final TransactionException VERSION_UNAVAILABLE_EXCEPTION = new TransactionException(
			"Fail on retrieveing an older and unexistent version.");

	public static int MAX_VERSIONS = Integer.getInteger(
			"org.deuce.transaction.mvstm.versions", 16);

	final private static AtomicInteger threadID = new AtomicInteger(0);

	private final int id;

	private static final AtomicInteger clock = new AtomicInteger(0);

	private int localClock;

	private ReadSet readSet = new ReadSet();
	private WriteSet writeSet = new WriteSet();

	// Keep per-thread read-only hints (uses more memory but faster)
	private final BooleanArrayList readWriteMarkers = new BooleanArrayList();
	private boolean readWriteHint = true;
	private int atomicBlockId;

	private final SharedLockProcedure lockProcedure = new SharedLockProcedure();

	public Context()
	{
		this.id = threadID.incrementAndGet();
		lockProcedure.owner = id;
	}

	public void init(int blockId, String metainf)
	{
		atomicBlockId = blockId;
		readWriteHint = readWriteMarkers.get(atomicBlockId);

		this.readSet.clear();
		this.writeSet.clear();
		this.localClock = clock.get();

		this.objectPool.clear();
		this.booleanPool.clear();
		this.bytePool.clear();
		this.charPool.clear();
		this.shortPool.clear();
		this.intPool.clear();
		this.longPool.clear();
		this.floatPool.clear();
		this.doublePool.clear();

		lockProcedure.clear();

	}

	TObjectProcedure<WriteFieldAccess> putProcedure = new TObjectProcedure<WriteFieldAccess>()
	{

		public boolean execute(WriteFieldAccess wfa)
		{
			wfa.put(localClock);
			return true;
		}
	};

	public boolean commit()
	{

		if (readWriteHint)
		{
			if (!writeSet.isEmpty())
			{
				try
				{
					writeSet.forEach(lockProcedure);
					if (readSet.validate(id))
					{
						localClock = clock.get() + 1;
						writeSet.forEach(putProcedure);
						clock.incrementAndGet();
					}
					else
					{
						return false;
					}
				}
				catch (TransactionException e)
				{
					return false;
				}
				finally
				{
					writeSet.forEach(lockProcedure.unlockProcedure);
				}
			}
		}
		return true;
	}

	public void rollback()
	{
	}

	public void beforeReadAccess(TxField field)
	{
	}

	private ReadFieldAccess dummy = new ReadFieldAccess();

	private WriteFieldAccess readLocal(VBoxField vbox)
	{
		dummy.init(vbox);
		return writeSet.contains(dummy);
	}

	public Object onReadAccess(Object value, TxField field)
	{
		VBoxField box = (VBoxField) field;
		Version ver = null;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return ((WriteFieldAccess) res).value;
			}
			ver = (Version) box.get(localClock);
			readSet.getNext().init(box, ver);
		}
		else
		{
			ver = (Version) box.get(localClock);
		}

		return ver == box.getTop() ? value : ver.value;
	}

	public boolean onReadAccess(boolean value, TxField field)
	{
		VBoxField box = (VBoxField) field;

		Version ver = null;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (Boolean) ((WriteFieldAccess) res).value;
			}
			ver = (Version) box.get(localClock);
			readSet.getNext().init(box, ver);
		}
		else
		{
			ver = (Version) box.get(localClock);
		}

		return (Boolean) (ver == box.getTop() ? value : ver.value);
	}

	public byte onReadAccess(byte value, TxField field)
	{
		VBoxField box = (VBoxField) field;
		Version ver = null;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (Byte) ((WriteFieldAccess) res).value;
			}
			ver = (Version) box.get(localClock);
			readSet.getNext().init(box, ver);
		}
		else
		{
			ver = (Version) box.get(localClock);
		}

		return (Byte) (ver == box.getTop() ? value : ver.value);
	}

	public char onReadAccess(char value, TxField field)
	{
		VBoxField box = (VBoxField) field;
		Version ver = null;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (Character) ((WriteFieldAccess) res).value;
			}
			ver = (Version) box.get(localClock);
			readSet.getNext().init(box, ver);
		}
		else
		{
			ver = (Version) box.get(localClock);
		}

		return (Character) (ver == box.getTop() ? value : ver.value);
	}

	public short onReadAccess(short value, TxField field)
	{
		VBoxField box = (VBoxField) field;
		Version ver = null;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (Short) ((WriteFieldAccess) res).value;
			}
			ver = (Version) box.get(localClock);
			readSet.getNext().init(box, ver);
		}
		else
		{
			ver = (Version) box.get(localClock);
		}

		return (Short) (ver == box.getTop() ? value : ver.value);
	}

	public int onReadAccess(int value, TxField field)
	{
		VBoxField box = (VBoxField) field;
		Version ver = null;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (Integer) ((WriteFieldAccess) res).value;
			}
			ver = (Version) box.get(localClock);
			readSet.getNext().init(box, ver);
		}
		else
		{
			ver = (Version) box.get(localClock);
		}

		return (Integer) (ver == box.getTop() ? value : ver.value);
	}

	public long onReadAccess(long value, TxField field)
	{
		VBoxField box = (VBoxField) field;
		Version ver = null;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (Long) ((WriteFieldAccess) res).value;
			}
			ver = (Version) box.get(localClock);
			readSet.getNext().init(box, ver);
		}
		else
		{
			ver = (Version) box.get(localClock);
		}

		return (Long) (ver == box.getTop() ? value : ver.value);
	}

	public float onReadAccess(float value, TxField field)
	{
		VBoxField box = (VBoxField) field;
		Version ver = null;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (Float) ((WriteFieldAccess) res).value;
			}
			ver = (Version) box.get(localClock);
			readSet.getNext().init(box, ver);
		}
		else
		{
			ver = (Version) box.get(localClock);
		}

		return (Float) (ver == box.getTop() ? value : ver.value);
	}

	public double onReadAccess(double value, TxField field)
	{
		VBoxField box = (VBoxField) field;
		Version ver = null;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (Double) ((WriteFieldAccess) res).value;
			}
			ver = (Version) box.get(localClock);
			readSet.getNext().init(box, ver);
		}
		else
		{
			ver = (Version) box.get(localClock);
		}

		return (Double) (ver == box.getTop() ? value : ver.value);
	}

	public void onWriteAccess(Object value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = objectPool.getNext();
		write.init((VBoxField) field);
		write.value = value;
		writeSet.put(write);
	}

	public void onWriteAccess(boolean value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = booleanPool.getNext();
		write.init((VBoxField) field);
		write.value = value;
		writeSet.put(write);
	}

	public void onWriteAccess(byte value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = bytePool.getNext();
		write.init((VBoxField) field);
		write.value = value;
		writeSet.put(write);
	}

	public void onWriteAccess(char value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = charPool.getNext();
		write.init((VBoxField) field);
		write.value = value;
		writeSet.put(write);
	}

	public void onWriteAccess(short value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = shortPool.getNext();
		write.init((VBoxField) field);
		write.value = value;
		writeSet.put(write);
	}

	public void onWriteAccess(int value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = intPool.getNext();
		write.init((VBoxField) field);
		write.value = value;
		writeSet.put(write);
	}

	public void onWriteAccess(long value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = longPool.getNext();
		write.init((VBoxField) field);
		write.value = value;
		writeSet.put(write);
	}

	public void onWriteAccess(float value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = floatPool.getNext();
		write.init((VBoxField) field);
		write.value = value;
		writeSet.put(write);
	}

	public void onWriteAccess(double value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = doublePool.getNext();
		write.init((VBoxField) field);
		write.value = value;
		writeSet.put(write);
	}

	private static class ObjectResourceFactory implements
			ResourceFactory<WriteFieldAccess>
	{
		@Override
		public WriteFieldAccess newInstance()
		{
			return new WriteFieldAccess();
		}
	}

	final private Pool<WriteFieldAccess> objectPool = new Pool<WriteFieldAccess>(
			new ObjectResourceFactory());

	private static class BooleanResourceFactory implements
			ResourceFactory<WriteFieldAccess>
	{
		@Override
		public WriteFieldAccess newInstance()
		{
			return new WriteFieldAccess();
		}
	}

	final private Pool<WriteFieldAccess> booleanPool = new Pool<WriteFieldAccess>(
			new BooleanResourceFactory());

	private static class ByteResourceFactory implements
			ResourceFactory<WriteFieldAccess>
	{
		@Override
		public WriteFieldAccess newInstance()
		{
			return new WriteFieldAccess();
		}
	}

	final private Pool<WriteFieldAccess> bytePool = new Pool<WriteFieldAccess>(
			new ByteResourceFactory());

	private static class CharResourceFactory implements
			ResourceFactory<WriteFieldAccess>
	{
		@Override
		public WriteFieldAccess newInstance()
		{
			return new WriteFieldAccess();
		}
	}

	final private Pool<WriteFieldAccess> charPool = new Pool<WriteFieldAccess>(
			new CharResourceFactory());

	private static class ShortResourceFactory implements
			ResourceFactory<WriteFieldAccess>
	{
		@Override
		public WriteFieldAccess newInstance()
		{
			return new WriteFieldAccess();
		}
	}

	final private Pool<WriteFieldAccess> shortPool = new Pool<WriteFieldAccess>(
			new ShortResourceFactory());

	private static class IntResourceFactory implements
			ResourceFactory<WriteFieldAccess>
	{
		@Override
		public WriteFieldAccess newInstance()
		{
			return new WriteFieldAccess();
		}
	}

	final private Pool<WriteFieldAccess> intPool = new Pool<WriteFieldAccess>(
			new IntResourceFactory());

	private static class LongResourceFactory implements
			ResourceFactory<WriteFieldAccess>
	{
		@Override
		public WriteFieldAccess newInstance()
		{
			return new WriteFieldAccess();
		}
	}

	final private Pool<WriteFieldAccess> longPool = new Pool<WriteFieldAccess>(
			new LongResourceFactory());

	private static class FloatResourceFactory implements
			ResourceFactory<WriteFieldAccess>
	{
		@Override
		public WriteFieldAccess newInstance()
		{
			return new WriteFieldAccess();
		}
	}

	final private Pool<WriteFieldAccess> floatPool = new Pool<WriteFieldAccess>(
			new FloatResourceFactory());

	private static class DoubleResourceFactory implements
			ResourceFactory<WriteFieldAccess>
	{
		@Override
		public WriteFieldAccess newInstance()
		{
			return new WriteFieldAccess();
		}
	}

	final private Pool<WriteFieldAccess> doublePool = new Pool<WriteFieldAccess>(
			new DoubleResourceFactory());

	@Override
	public void onIrrevocableAccess()
	{
	}

	@Override
	public ArrayContainer onReadAccess(ArrayContainer value, TxField field)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onWriteAccess(ArrayContainer value, TxField field)
	{
		// TODO FAZER

	}
}
