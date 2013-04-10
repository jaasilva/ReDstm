package org.deuce.transaction.score;

import org.deuce.distribution.replication.group.Group;
import org.deuce.transaction.ReadSet;
import org.deuce.transaction.score.field.ReadFieldAccess;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReReadSet extends ReadSet
{
	private static final long serialVersionUID = -8242797020618563511L;
	protected static final int DEFAULT_CAPACITY = 1024;
	protected ReadFieldAccess[] readSet = new ReadFieldAccess[DEFAULT_CAPACITY];
	protected int nextAvaliable = 0;

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

	public ReadFieldAccess scoreGetNext()
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

	public int size()
	{
		return nextAvaliable;
	}

	public Group getInvolvedNodes()
	{
		// TODO Auto-generated constructor stub
		return null;
	}
}
