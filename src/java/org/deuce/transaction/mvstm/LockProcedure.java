package org.deuce.transaction.mvstm;

import org.deuce.transaction.mvstm.field.InPlaceLock;
import org.deuce.transaction.mvstm.field.WriteFieldAccess;
import org.deuce.transform.ExcludeTM;
import org.deuce.trove.TObjectProcedure;

/**
 * Procedure used to scan the WriteSet on commit.
 * 
 * @author Guy, Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 * @since 1.4
 */
@ExcludeTM
public class LockProcedure implements TObjectProcedure<WriteFieldAccess>
{
	public int i = 0;
	public Context owner;

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

	public LockProcedure(Context locker)
	{
		this.owner = locker;
	}

	@Override
	public boolean execute(WriteFieldAccess writeField)
	{
		if (((InPlaceLock) writeField.field).lock(owner))
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
