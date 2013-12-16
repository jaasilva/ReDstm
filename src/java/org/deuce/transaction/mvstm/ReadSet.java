package org.deuce.transaction.mvstm;

import java.io.Serializable;

import org.deuce.transaction.TransactionException;
import org.deuce.transaction.mvstm.field.ReadFieldAccess;
import org.deuce.transaction.mvstm.field.VBoxField;
import org.deuce.transform.ExcludeTM;

/**
 * Represents the transaction read set. And acts as a recycle pool of the
 * {@link ReadFieldAccess}.
 * 
 * @author Guy Korland, Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 * @since 0.7
 */
@ExcludeTM
public class ReadSet implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_CAPACITY = 1024;
	private ReadFieldAccess[] readSet = new ReadFieldAccess[DEFAULT_CAPACITY];
	private int nextAvaliable = 0;

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

	public boolean validate(Context owner)
	{
		for (int i = 0; i < nextAvaliable; i++)
		{
			ReadFieldAccess rfa = readSet[i];
			VBoxField field = (VBoxField) rfa.field;
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
			if (!field.validate(rfa.version, owner))
			{
				return false;
			}
		}
		return true;
	}
}
