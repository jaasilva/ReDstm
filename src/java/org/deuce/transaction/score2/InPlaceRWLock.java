package org.deuce.transaction.score2;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
public interface InPlaceRWLock
{
	boolean exlusiveLock();

	void exclusiveUnlock();

	boolean sharedLock();

	void sharedUnlock();
}
