package org.deuce.transaction.tl2;

import org.deuce.transaction.TransactionException;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class LockTable
{

	// Failure transaction
	final public static TransactionException FAILURE_EXCEPTION = new TransactionException(
			"Faild on lock.");
	final public static int LOCK = 1 << 31;
	final public static int UNLOCK = ~LOCK;

}
