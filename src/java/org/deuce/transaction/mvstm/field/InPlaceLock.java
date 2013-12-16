package org.deuce.transaction.mvstm.field;

import org.deuce.transaction.DistributedContext;
import org.deuce.transform.ExcludeTM;

/**
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public interface InPlaceLock
{
	boolean lock(DistributedContext owner);

	void unLock();
}
