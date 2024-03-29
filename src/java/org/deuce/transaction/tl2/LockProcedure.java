package org.deuce.transaction.tl2;

import org.deuce.transaction.tl2.field.WriteFieldAccess;
import org.deuce.transform.ExcludeTM;
import org.deuce.trove.TObjectProcedure;

/**
 * Procedure used to scan the WriteSet on commit.
 * 
 * @author Guy
 * @since 1.4
 */
@ExcludeTM
public class LockProcedure implements TObjectProcedure<WriteFieldAccess>
{
	private int i = 0;
	private Context locker;

	public LockProcedure(Context locker)
	{
		setAndUnlockProcedure = new SetAndUnlockProcedure();
		this.locker = locker;
	}

	@Override
	public boolean execute(WriteFieldAccess writeField)
	{
		if (((InPlaceLock) writeField.field).lock(locker))
		{
			i++;
		}
		return true;
	}

	public void clear()
	{
		this.i = 0;
	}

	public final TObjectProcedure<WriteFieldAccess> unlockProcedure = new TObjectProcedure<WriteFieldAccess>()
	{
		@Override
		public boolean execute(WriteFieldAccess field)
		{
			if (i > 0)
			{
				((InPlaceLock) field.field).unLock();
				i--;
				return true;
			}
			return false;
		}
	};

	public static class SetAndUnlockProcedure implements
			TObjectProcedure<WriteFieldAccess>
	{
		private int newClock;

		@Override
		public boolean execute(WriteFieldAccess value)
		{
			((InPlaceLock) value.field).setAndReleaseLock(newClock);
			return true;
		}

		public void retrieveNewClock()
		{
			this.newClock = Context.clock.incrementAndGet();
		}
	}

	public final SetAndUnlockProcedure setAndUnlockProcedure;
}
