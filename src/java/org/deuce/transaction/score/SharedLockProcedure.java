package org.deuce.transaction.score;

import org.deuce.transaction.score.field.WriteFieldAccess;
import org.deuce.trove.TObjectProcedure;

/**
 * Procedure used to scan the WriteSet on commit.
 * 
 * @author Guy, Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 * @since 1.4
 */
public class SharedLockProcedure implements TObjectProcedure<WriteFieldAccess>
{
	public int i = 0;
	public int owner;

	public final TObjectProcedure<WriteFieldAccess> unlockProcedure = new TObjectProcedure<WriteFieldAccess>()
	{
		public boolean execute(WriteFieldAccess field)
		{
			if (i > 0)
			{
				((InPlaceRWLock) field.field).sharedUnlock();
				i--;
				return true;
			}
			return false;
		}
	};

	public SharedLockProcedure()
	{
	}

	public boolean execute(WriteFieldAccess writeField)
	{
		if (((InPlaceRWLock) writeField.field).sharedLock())
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
