package org.deuce.transaction.score;

/**
 * 
 * @author jaasilva
 */
public interface InPlaceRWLock
{
	boolean exclusiveLock();

	void exclusiveUnlock();

	boolean sharedLock();

	void sharedUnlock();
}
