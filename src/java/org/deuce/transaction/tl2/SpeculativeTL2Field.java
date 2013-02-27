package org.deuce.transaction.tl2;

import org.deuce.reflection.AddressUtil;
import org.deuce.reflection.UnsafeHolder;
import org.deuce.transaction.DistributedContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.speculative.SpeculativeTxField;

@ExcludeTM
public class SpeculativeTL2Field extends SpeculativeTxField implements
		InPlaceLock, java.io.Serializable
{
	private static long __LOCK_FIELD__;
	static
	{
		try
		{
			__LOCK_FIELD__ = AddressUtil.getAddress(TL2Field.class
					.getDeclaredField("lock"));
		}
		catch (SecurityException e)
		{
		}
		catch (NoSuchFieldException e)
		{
		}
	}
	public volatile int lock = 0;
	private volatile SpeculativeContext lockHolder; // Object reference

	public SpeculativeTL2Field()
	{
	}

	public SpeculativeTL2Field(Object ref, long address)
	{
		super(ref, address);
	}

	@Override
	public void init(Object ref, long address)
	{
		super.init(ref, address);
		lock = 0;
	}

	public int checkLock(int clock)
	{
		int l = lock;
		if (clock < (l & LockTable.UNLOCK) || (l & LockTable.LOCK) != 0)
		{
			throw LockTable.FAILURE_EXCEPTION;
		}

		return l;
	}

	public int checkLock(int clock, DistributedContext lockChecker)
	{
		int l = lock;
		SpeculativeContext lh = lockHolder;

		if (clock < (l & LockTable.UNLOCK))
			throw LockTable.FAILURE_EXCEPTION;

		// already locked, and not by lockChecker
		if (((l & LockTable.LOCK) != 0)
				&& (lh != (SpeculativeContext) lockChecker))
		{
			throw LockTable.FAILURE_EXCEPTION;
		}

		return l;
	}

	public void checkLock(int clock, int expected)
	{
		int l = lock;
		if (l != expected || clock < (l & LockTable.UNLOCK)
				|| (l & LockTable.LOCK) != 0)
		{
			throw LockTable.FAILURE_EXCEPTION;
		}
	}

	public boolean lock(DistributedContext locker)
	{
		int l = lock;

		if ((l & LockTable.LOCK) != 0)
		{
			throw LockTable.FAILURE_EXCEPTION;
		}

		boolean isLocked = UnsafeHolder.getUnsafe().compareAndSwapInt(this,
				__LOCK_FIELD__, l, l | LockTable.LOCK);

		if (!isLocked)
		{
			throw LockTable.FAILURE_EXCEPTION;
		}

		lockHolder = (SpeculativeContext) locker; // mark as "locked by"

		return true;
	}

	public void setAndReleaseLock(int newClock)
	{
		lockHolder = null;
		lock = newClock;
	}

	public void unLock()
	{
		lockHolder = null;
		lock = lock & LockTable.UNLOCK;
	}

	public void checkLock2(int clock)
	{
		int l = lock;
		if ((l & LockTable.LOCK) != 0 || clock < (l & LockTable.UNLOCK))
		{
			throw LockTable.FAILURE_EXCEPTION;
		}

	}
}
