package org.deuce.transaction.tl2;

import org.deuce.transaction.DistributedContext;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public interface InPlaceLock {
	
	boolean lock(DistributedContext locker);
	int checkLock(int clock);
	int checkLock(int clock, DistributedContext lockChecker);
	void checkLock2(int clock);
	void checkLock(int clock, int expected);
	void unLock();
	void setAndReleaseLock(int newClock);
	
}
