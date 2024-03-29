package org.deuce.transaction.mvstm;

import org.deuce.transaction.TransactionException;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class LockTable
{
	final public static TransactionException LOCKED_VERSION_EXCEPTION = new TransactionException(
			"Faild on locking version."); // Failure transaction
	final public static int LOCK = 1 << 31;
	final public static int UNLOCK = ~LOCK;
}
