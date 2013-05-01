package org.deuce.transaction.tl2.speculative;

import org.deuce.transaction.field.WriteFieldAccess;
import org.deuce.transaction.tl2.InPlaceLock;
import org.deuce.transform.ExcludeTM;
import org.deuce.trove.TObjectProcedure;

/**
 * Procedure used to scan the WriteSet on commit.
 * 
 * @author Guy
 * @since 1.4
 */
@ExcludeTM
public class SpeculativeLockProcedure implements
		TObjectProcedure<WriteFieldAccess>
{

	private int i = 0;
	private SpeculativeContext locker;

	public final TObjectProcedure<WriteFieldAccess> unlockProcedure = new TObjectProcedure<WriteFieldAccess>()
	{
		public boolean execute(WriteFieldAccess wfa)
		{
			if (i > 0)
			{
				((InPlaceLock) wfa.field).unLock();
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

		public boolean execute(WriteFieldAccess wfa)
		{
			((InPlaceLock) wfa.field).setAndReleaseLock(newClock);
			return true;
		}

		public void retrieveNewClock()
		{
			this.newClock = SpeculativeContext.clock.incrementAndGet();
		}
	}

	public final SetAndUnlockProcedure setAndUnlockProcedure;

	public SpeculativeLockProcedure(SpeculativeContext locker)
	{
		setAndUnlockProcedure = new SetAndUnlockProcedure();
		this.locker = locker;
	}

	public boolean execute(WriteFieldAccess wfa)
	{
		if (((InPlaceLock) wfa.field).lock(locker))
		{
			i++;
		}
		return true;
	}

	public void clear()
	{
		this.i = 0;
	}

}
