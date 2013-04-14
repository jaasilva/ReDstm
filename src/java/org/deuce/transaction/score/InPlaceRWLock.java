package org.deuce.transaction.score;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
public interface InPlaceRWLock
{
	boolean exclusiveLock();

	void exclusiveUnlock();

	boolean sharedLock();

	void sharedUnlock();
}
