package org.deuce.transaction.tl2;

import org.deuce.reflection.AddressUtil;
import org.deuce.reflection.UnsafeHolder;
import org.deuce.transaction.DistributedContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class TL2Field extends TxField implements InPlaceLock,
		java.io.Serializable
{
	private static long __LOCK_FIELD__;
	static
	{
		try
		{
			__LOCK_FIELD__ = AddressUtil.getAddress(TL2Field.class
					.getDeclaredField("lock"));
		}
		catch (Exception e)
		{
		}
	}
	public volatile int lock = 0;
	private transient volatile Context lockHolder; // Object reference

	public TL2Field()
	{
	}

	public TL2Field(Object ref, long address)
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
		if ((clock < (l & LockTable.UNLOCK)) || ((l & LockTable.LOCK) != 0))
		{ // Abort tx. (if already updated or locked)
			throw LockTable.FAILURE_EXCEPTION;
		}

		return l;
	}

	public int checkLock(int clock, DistributedContext lockChecker)
	{
		int l = lock;
		Context lh = lockHolder;

		if (clock < (l & LockTable.UNLOCK))
		{
			throw LockTable.FAILURE_EXCEPTION;
		}

		if (((l & LockTable.LOCK) != 0) && (lh != (Context) lockChecker))
		{ // already locked, and not by lockChecker
			throw LockTable.FAILURE_EXCEPTION;
		}

		return l;
	}

	public void checkLock(int clock, int expected)
	{
		int l = lock;
		if (l != expected || clock < (l & LockTable.UNLOCK))
		{
			throw LockTable.FAILURE_EXCEPTION;
		}
		if ((l & LockTable.LOCK) != 0)
		{
			throw LockTable.FAILURE_EXCEPTION;
		}
	}

	public boolean lock(DistributedContext locker)
	{
		int l = lock;

		if ((l & LockTable.LOCK) != 0)
		{ // already locked
			throw LockTable.FAILURE_EXCEPTION;
		}

		boolean isLocked = UnsafeHolder.getUnsafe().compareAndSwapInt(this,
				__LOCK_FIELD__, l, l | LockTable.LOCK);

		if (!isLocked)
		{
			throw LockTable.FAILURE_EXCEPTION;
		}

		lockHolder = (Context) locker; // mark as "locked by"

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

		if (clock < (l & LockTable.UNLOCK))
		{
			throw LockTable.FAILURE_EXCEPTION;
		}
		if ((l & LockTable.LOCK) != 0)
		{
			throw LockTable.FAILURE_EXCEPTION;
		}
	}
}
