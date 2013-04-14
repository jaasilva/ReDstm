package org.deuce.transaction.score;

import java.io.Serializable;

import org.deuce.distribution.replication.group.Group;
import org.deuce.transaction.score.field.ReadFieldAccess;
import org.deuce.transaction.score.field.WriteFieldAccess;
import org.deuce.transform.ExcludeTM;
import org.deuce.trove.THashSet;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReWriteSet implements Serializable
{
	private static final long serialVersionUID = 3147866584383152424L;
	final private THashSet<WriteFieldAccess> writeSet = new THashSet<WriteFieldAccess>(
			16);

	public void clear()
	{
		writeSet.clear();
	}

	public boolean isEmpty()
	{
		return writeSet.isEmpty();
	}

	public Group getInvolvedNodes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public WriteFieldAccess contains(ReadFieldAccess read)
	{ // Check if it is already included in the write set
		return writeSet.get(read);
	}

	public void put(WriteFieldAccess write)
	{ // Add to write set
		if (!writeSet.add(write))
			writeSet.replace(write);
	}

	public int size()
	{
		return writeSet.size();
	}

	public void releaseExclusiveLocks()
	{ // assumes that these locks are hold
		for (WriteFieldAccess a : writeSet)
		{
			((InPlaceRWLock) a.field).exclusiveUnlock();
		}
	}

	public boolean getExclusiveLocks()
	{ // CHECKME does this work? is there a better way?
		boolean res = true;
		int i = 0;
		WriteFieldAccess[] ws = (WriteFieldAccess[]) writeSet.toArray();
		while (res)
		{
			res = ((InPlaceRWLock) ws[i].field).sharedLock();
			i++;
		}

		if (!res)
		{
			for (int j = i - 1; j >= 0; j--)
			{
				((InPlaceRWLock) ws[i].field).sharedUnlock();
			}
		}

		return res;
	}
}
