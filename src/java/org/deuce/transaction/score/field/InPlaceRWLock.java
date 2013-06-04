package org.deuce.transaction.score.field;

/**
 * 
 * @author jaasilva
 */
public interface InPlaceRWLock
{
	boolean exclusiveLock(String holder);

	boolean exclusiveUnlock(String holder);

	boolean sharedLock(String holder);

	boolean sharedUnlock(String holder);

	boolean isExclusiveUnlocked();
}
