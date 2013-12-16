package org.deuce.transaction.mvstm;

import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.Defaults;
import org.deuce.LocalMetadata;
import org.deuce.distribution.TribuDSTM;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.mvstm.field.ReadFieldAccess;
import org.deuce.transaction.mvstm.field.VBoxField;
import org.deuce.transaction.mvstm.field.Version;
import org.deuce.transaction.mvstm.field.WriteFieldAccess;
import org.deuce.transaction.pool.Pool;
import org.deuce.transaction.pool.ResourceFactory;
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
 * @author Ricardo Dias
 * @version Oct 11, 2010 15:00:19 PM
 */
@ExcludeTM
@LocalMetadata(metadataClass = "org.deuce.transaction.mvstm.field.VBoxField")
public final class Context extends DistributedContext
{
	private static final TransactionException READ_ONLY_FAILURE_EXCEPTION = new TransactionException(
			"Fail on write (read-only hint was set).");
	public static final TransactionException VERSION_UNAVAILABLE_EXCEPTION = new TransactionException(
			"Fail on retrieveing an older and unexistent version.");

	public static int MAX_VERSIONS = Integer
			.getInteger(Defaults._MVSTM_MVCC_MAX_VERSIONS,
					Defaults.MVSTM_MVCC_MAX_VERSIONS);

	private ReadSet readSet;
	private WriteSet writeSet;

	private static final AtomicInteger clock = new AtomicInteger(0);
	private int localClock;

	// Keep per-thread read-only hints (uses more memory but faster)
	private final BooleanArrayList readWriteMarkers = new BooleanArrayList();
	private boolean readWriteHint = true;

	private final LockProcedure lockProcedure = new LockProcedure(this);
	private final TObjectProcedure<WriteFieldAccess> putProcedure = new TObjectProcedure<WriteFieldAccess>()
	{
		public boolean execute(WriteFieldAccess wfa)
		{
			wfa.put(localClock);
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

	@Override
	public void recreateContextFromState(DistributedContextState ctxState)
	{
		super.recreateContextFromState(ctxState);

		readSet = (ReadSet) ctxState.rs;
		writeSet = (WriteSet) ctxState.ws;

		localClock = ((ContextState) ctxState).rv;

		atomicBlockId = -1; // Remote tx

		WFAPool.clear();
	}

	@Override
	public DistributedContextState createState()
	{
		return new ContextState(readSet, writeSet, threadID, atomicBlockId,
				localClock, readWriteHint);
	}

	@Override
	public void initialise(int atomicBlockId, String metainf)
	{
		readWriteHint = readWriteMarkers.get(atomicBlockId);

		readSet.clear();
		writeSet.clear();

		localClock = clock.get();

		WFAPool.clear();

		lockProcedure.clear();
	}

	@Override
	protected boolean performValidation()
	{
		boolean result = false;

		try
		{
			writeSet.forEach(lockProcedure);
			result = readSet.validate(this);
		}
		catch (TransactionException e)
		{
			writeSet.forEach(lockProcedure.unlockProcedure);
			return false;
		}

		return result;
	}

	@Override
	protected void applyUpdates()
	{
		localClock = clock.get() + 1;
		writeSet.forEach(putProcedure);

		clock.incrementAndGet();
		writeSet.forEach(lockProcedure.unlockProcedure);

		lockProcedure.clear();
	}

	protected Version lastReadVersion;

	public void beforeReadAccess(TxField field)
	{
		lastReadVersion = ((VBoxField) field).get(localClock);
	}

	private ReadFieldAccess dummy = new ReadFieldAccess();

	private WriteFieldAccess readLocal(VBoxField vbox)
	{
		dummy.init(vbox);
		return writeSet.contains(dummy);
	}

	/***************************************
	 * ON READ ACCESS
	 **************************************/

	public ArrayContainer onReadAccess(ArrayContainer value, TxField field)
	{
		VBoxField box = (VBoxField) field;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (ArrayContainer) res.value;
			}
			readSet.getNext().init(box, lastReadVersion);
		}

		return (ArrayContainer) (lastReadVersion == box.getLastVersion() ? value
				: lastReadVersion.value);
	}

	public Object onReadAccess(Object value, TxField field)
	{
		VBoxField box = (VBoxField) field;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return res.value;
			}
			readSet.getNext().init(box, lastReadVersion);
		}

		return lastReadVersion == box.getLastVersion() ? value
				: lastReadVersion.value;
	}

	public boolean onReadAccess(boolean value, TxField field)
	{
		VBoxField box = (VBoxField) field;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (boolean) res.value;
			}
			readSet.getNext().init(box, lastReadVersion);
		}

		return (boolean) (lastReadVersion == box.getLastVersion() ? value
				: lastReadVersion.value);
	}

	public byte onReadAccess(byte value, TxField field)
	{
		VBoxField box = (VBoxField) field;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (byte) res.value;
			}
			readSet.getNext().init(box, lastReadVersion);
		}

		return (byte) (lastReadVersion == box.getLastVersion() ? value
				: lastReadVersion.value);
	}

	public char onReadAccess(char value, TxField field)
	{
		VBoxField box = (VBoxField) field;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (char) res.value;
			}
			readSet.getNext().init(box, lastReadVersion);
		}

		return (char) (lastReadVersion == box.getLastVersion() ? value
				: lastReadVersion.value);
	}

	public short onReadAccess(short value, TxField field)
	{
		VBoxField box = (VBoxField) field;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (short) res.value;
			}
			readSet.getNext().init(box, lastReadVersion);
		}

		return (short) (lastReadVersion == box.getLastVersion() ? value
				: lastReadVersion.value);
	}

	public int onReadAccess(int value, TxField field)
	{
		VBoxField box = (VBoxField) field;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (int) res.value;
			}
			readSet.getNext().init(box, lastReadVersion);
		}

		return (int) (lastReadVersion == box.getLastVersion() ? value
				: lastReadVersion.value);
	}

	public long onReadAccess(long value, TxField field)
	{
		VBoxField box = (VBoxField) field;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (long) res.value;
			}
			readSet.getNext().init(box, lastReadVersion);
		}

		return (long) (lastReadVersion == box.getLastVersion() ? value
				: lastReadVersion.value);
	}

	public float onReadAccess(float value, TxField field)
	{
		VBoxField box = (VBoxField) field;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (float) res.value;
			}
			readSet.getNext().init(box, lastReadVersion);
		}

		return (float) (lastReadVersion == box.getLastVersion() ? value
				: lastReadVersion.value);
	}

	public double onReadAccess(double value, TxField field)
	{
		VBoxField box = (VBoxField) field;

		if (readWriteHint)
		{
			WriteFieldAccess res = readLocal(box);
			if (res != null)
			{
				return (double) res.value;
			}
			readSet.getNext().init(box, lastReadVersion);
		}

		return (double) (lastReadVersion == box.getLastVersion() ? value
				: lastReadVersion.value);
	}

	/***************************************
	 * ON WRITE ACCESS
	 **************************************/

	public void onWriteAccess(ArrayContainer value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = WFAPool.getNext();
		write.set(value, field);
		writeSet.put(write);
	}

	public void onWriteAccess(Object value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = WFAPool.getNext();
		write.set(value, field);
		writeSet.put(write);
	}

	public void onWriteAccess(boolean value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = WFAPool.getNext();
		write.set(value, field);
		writeSet.put(write);
	}

	public void onWriteAccess(byte value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = WFAPool.getNext();
		write.set(value, field);
		writeSet.put(write);
	}

	public void onWriteAccess(char value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = WFAPool.getNext();
		write.set(value, field);
		writeSet.put(write);
	}

	public void onWriteAccess(short value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = WFAPool.getNext();
		write.set(value, field);
		writeSet.put(write);
	}

	public void onWriteAccess(int value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = WFAPool.getNext();
		write.set(value, field);
		writeSet.put(write);
	}

	public void onWriteAccess(long value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = WFAPool.getNext();
		write.set(value, field);
		writeSet.put(write);
	}

	public void onWriteAccess(float value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = WFAPool.getNext();
		write.set(value, field);
		writeSet.put(write);
	}

	public void onWriteAccess(double value, TxField field)
	{
		if (!readWriteHint)
		{
			readWriteMarkers.insert(atomicBlockId, true);
			throw READ_ONLY_FAILURE_EXCEPTION;
		}

		WriteFieldAccess write = WFAPool.getNext();
		write.set(value, field);
		writeSet.put(write);
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

	@Override
	public void onIrrevocableAccess()
	{
	}

	public boolean commit()
	{
		if (writeSet.isEmpty() || !readWriteHint)
		{
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
