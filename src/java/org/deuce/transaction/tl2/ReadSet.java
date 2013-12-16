package org.deuce.transaction.tl2;

import java.io.Serializable;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.tl2.field.ReadFieldAccess;
import org.deuce.transform.ExcludeTM;

/**
 * Represents the transaction read set. And acts as a recycle pool of the
 * {@link ReadFieldAccess}.
 * 
 * @author Guy Korland
 * @since 0.7
 */
@ExcludeTM
public class ReadSet implements Serializable
{
	private static final long serialVersionUID = 8775391877670448988L;
	protected static final int DEFAULT_CAPACITY = 1024;
	protected ReadFieldAccess[] readSet = new ReadFieldAccess[DEFAULT_CAPACITY];
	protected int nextAvaliable = 0;

	public ReadSet()
	{
		fillArray(0);
	}

	public void clear()
	{
		nextAvaliable = 0;
	}

	private void fillArray(int offset)
	{
		for (int i = offset; i < readSet.length; ++i)
		{
			readSet[i] = new ReadFieldAccess();
		}
	}

	public ReadFieldAccess getNext()
	{
		if (nextAvaliable >= readSet.length)
		{
			int orignLength = readSet.length;
			ReadFieldAccess[] tmpReadSet = new ReadFieldAccess[2 * orignLength];
			System.arraycopy(readSet, 0, tmpReadSet, 0, orignLength);
			readSet = tmpReadSet;
			fillArray(orignLength);
		}
		return readSet[nextAvaliable++];
	}

	public int size()
	{
		return nextAvaliable;
	}

	public void checkClock(int clock, Context lockChecker)
	{
		for (int i = 0; i < nextAvaliable; i++)
		{
			InPlaceLock field = (InPlaceLock) readSet[i].field;
			if (field == null)
			{
				/*
				 * This means we received a TxField that has already been
				 * garbage collected in this node. This implies that its owner
				 * has also already been collected. Therefore, the owner has
				 * been removed by some previous transaction and therefore it is
				 * safe to abort this one.
				 */
				throw new TransactionException(
						"Received already collected metadata.");
			}
			field.checkLock(clock, lockChecker);
			readSet[i].clear();
		}
	}
}
