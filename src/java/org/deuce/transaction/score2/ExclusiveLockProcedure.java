package org.deuce.transaction.score2;

import org.deuce.transaction.score2.field.WriteFieldAccess;
import org.deuce.trove.TObjectProcedure;

/**
 * Procedure used to scan the WriteSet on commit.
 * 
 * @author Guy, Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 * @since 1.4
 */
public class ExclusiveLockProcedure implements
		TObjectProcedure<WriteFieldAccess>
{
	public int i = 0;
	public int owner;

	public final TObjectProcedure<WriteFieldAccess> unlockProcedure = new TObjectProcedure<WriteFieldAccess>()
	{
		public boolean execute(WriteFieldAccess field)
		{
			if (i > 0)
			{
				((InPlaceRWLock) field.field).exclusiveUnlock();
				i--;
				return true;
			}
			return false;
		}
	};

	public ExclusiveLockProcedure()
	{
	}

	public boolean execute(WriteFieldAccess writeField)
	{
		if (((InPlaceRWLock) writeField.field).exlusiveLock())
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
