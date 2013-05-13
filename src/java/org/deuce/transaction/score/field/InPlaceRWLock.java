package org.deuce.transaction.score.field;

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

	boolean isExclusiveUnlocked();
}
