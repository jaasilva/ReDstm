package org.deuce.transaction.tl2.speculative;

import java.util.NoSuchElementException;

import org.deuce.transaction.ReadSet;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.field.ReadFieldAccess;
import org.deuce.transaction.speculative.SpeculativeContext;
import org.deuce.transaction.tl2.InPlaceLock;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.speculative.SpeculativeTxField;

/**
 * Represents the transaction read set. And acts as a recycle pool of the
 * {@link ReadFieldAccess}.
 * 
 * @author Guy Korland
 * @since 0.7
 */
@ExcludeTM
public class SpeculativeTL2ReadSet extends ReadSet
{
	private static final long serialVersionUID = 1L;

	public void checkClock(int clock)
	{
		for (int i = 0; i < nextAvaliable; i++)
		{
			InPlaceLock field = (InPlaceLock) readSet[i].field;
			if (field == null)
			{
				// This means we received a TxField that has already been
				// garbage collected in this node. This implies that its owner
				// has also already been collected. Therefore, the owner has
				// been removed by some previous transaction and therefore it is
				// safe to abort this one.
				throw new TransactionException(
						"Received already collected metadata.");
			}
			field.checkLock(clock);
			// readSet[i].clear();
		}
	}

	public void speculativeCheckClock(int clock)
	{
		for (int i = 0; i < nextAvaliable; i++)
		{
			SpeculativeTxField field = (SpeculativeTxField) readSet[i].field;

			if (field == null)
			{
				// This means we received a TxField that has already been
				// garbage collected in this node. This implies that its owner
				// has also already been collected. Therefore, the owner has
				// been removed by some previous transaction and therefore it is
				// safe to abort this one.
				throw new TransactionException(
						"Received already collected metadata.");
			}
			((InPlaceLock) field).checkLock(clock);

			try
			{
				if (!field.speculativeList.isEmpty()
						&& clock < field.speculativeList.getLast().ctx
								.getSpeculativeVersionNumber())
					throw SpeculativeContext.EARLY_CONFLICT;
			}
			catch (NoSuchElementException e)
			{
				// System.out.println("-- speculativeList was empty, and lock was not taken.");
			}
			finally
			{
				((InPlaceLock) field).checkLock(clock);
			}
		}
	}
}
