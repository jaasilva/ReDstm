package org.deuce.transaction.tl2;

import org.deuce.transaction.ReadSet;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.field.ReadFieldAccess;
import org.deuce.transform.ExcludeTM;

/**
 * Represents the transaction read set. And acts as a recycle pool of the
 * {@link ReadFieldAccess}.
 * 
 * @author Guy Korland
 * @since 0.7
 */
@ExcludeTM
public class TL2ReadSet extends ReadSet
{
	private static final long serialVersionUID = 1L;

	public void checkClock(int clock, Context lockChecker)
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
				// System.out.println("Received already collected metadata.");
				throw new TransactionException(
						"Received already collected metadata.");
			}
			field.checkLock(clock, lockChecker);
			readSet[i].clear();
		}
	}
}
