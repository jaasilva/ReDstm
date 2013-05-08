package org.deuce.transaction.score;

import java.io.Serializable;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;
import org.deuce.distribution.replication.partial.oid.PartialReplicationOID;
import org.deuce.transaction.score.field.SCOReReadFieldAccess;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReReadSet implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_CAPACITY = 1024;
	private SCOReReadFieldAccess[] readSet = new SCOReReadFieldAccess[DEFAULT_CAPACITY];
	private int next = 0;

	public SCOReReadSet()
	{ // CHECKME change array to hashSet?
		fillArray(0);
	}

	private void fillArray(int offset)
	{
		for (int i = offset; i < readSet.length; i++)
		{
			readSet[i] = new SCOReReadFieldAccess();
		}
	}

	public void clear()
	{
		next = 0;
	}

	public SCOReReadFieldAccess getNext()
	{
		if (next == readSet.length)
		{
			int originLength = readSet.length;
			SCOReReadFieldAccess[] tmp = new SCOReReadFieldAccess[2 * originLength];
			System.arraycopy(readSet, 0, tmp, 0, originLength);
			readSet = tmp;
			fillArray(originLength);
		}

		return readSet[next++];
	}

	public void releaseSharedLocks()
	{ // assumes that these locks are held
		for (int i = 0; i < next; i++)
		{
			((InPlaceRWLock) readSet[i].field).sharedUnlock();
		}
	}

	public boolean getSharedLocks()
	{
		boolean res = true;
		int i = 0;
		while (res && i < next)
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
	{ // validate only the TxFields that I replicate
		for (int i = 0; i < next; i++)
		{
			if (TribuDSTM
					.isLocalGroup(((PartialReplicationOID) readSet[i].field
							.getMetadata()).getGroup()))
			{
				if (readSet[i].field.getLastVersion().version > sid)
				{
					return false;
				}
			}
		}

		return true;
	}

	public Group getInvolvedNodes()
	{
		Group resGroup = new PartialReplicationGroup();

		for (int i = 0; i < next; i++)
		{
			Group other = ((PartialReplicationOID) readSet[i].field
					.getMetadata()).getGroup();

			if (!TribuDSTM.groupIsAll(other))
			{ // never do union with the group ALL
				resGroup = resGroup.union(other);
			}
		}

		return resGroup;
	}

	public String toString()
	{
		return readSet.toString();
	}
}
