package org.deuce.transaction.score;

import java.io.Serializable;

import org.deuce.distribution.replication.group.Group;
import org.deuce.transaction.score.field.ReadFieldAccess;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReReadSet implements Serializable
{
	private static final long serialVersionUID = -8242797020618563511L;
	private static final int DEFAULT_CAPACITY = 1024;
	private ReadFieldAccess[] readSet = new ReadFieldAccess[DEFAULT_CAPACITY];
	private int nextAvaliable = 0;

	public SCOReReadSet()
	{
		fillArray(0);
	}

	public void clear()
	{
		nextAvaliable = 0;
	}

	private void fillArray(int offset)
	{
		for (int i = offset; i < readSet.length; ++i)
		{
			readSet[i] = new ReadFieldAccess();
		}
	}

	public ReadFieldAccess getNext()
	{
		if (nextAvaliable >= readSet.length)
		{
			int orignLength = readSet.length;
			ReadFieldAccess[] tmpReadSet = new ReadFieldAccess[2 * orignLength];
			System.arraycopy(readSet, 0, tmpReadSet, 0, orignLength);
			readSet = tmpReadSet;
			fillArray(orignLength);
		}
		return readSet[nextAvaliable++];
	}

	public void releaseSharedLocks()
	{ // assumes that these locks are hold
		for (ReadFieldAccess a : readSet)
		{
			((InPlaceRWLock) a.field).sharedUnlock();
		}
	}

	public boolean getSharedLocks()
	{ // CHECKME is there a better way?
		boolean res = true;
		int i = 0;
		while (res)
		{
			res = ((InPlaceRWLock) readSet[i].field).sharedLock();
			i++;
		}

		if (!res)
		{
			for (int j = i - 1; j >= 0; j--)
			{
				((InPlaceRWLock) readSet[j].field).sharedUnlock();
			}
		}

		return res;
	}

	public boolean validate(int sid)
	{
		for (ReadFieldAccess rfa : readSet)
		{
			if (rfa.field.getLastVersion().version > sid)
			{
				return false;
			}
		}

		return true;
	}

	public Group getInvolvedNodes()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public int size()
	{
		return nextAvaliable;
	}
}
