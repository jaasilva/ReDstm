package org.deuce.transaction.score;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
public interface InPlaceLock
{
	boolean lock(int owner);

	void unLock();
}
