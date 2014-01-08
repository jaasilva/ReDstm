package org.deuce.transaction.score;

import java.io.Serializable;

import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;
import org.deuce.distribution.replication.partial.PartialReplicationOID;
import org.deuce.transaction.score.field.InPlaceRWLock;
import org.deuce.transaction.score.field.SCOReReadFieldAccess;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class SCOReReadSet implements Serializable
{
	private static final long serialVersionUID = 1L;
	private static final int DEFAULT_CAPACITY = 1024;
	private SCOReReadFieldAccess[] readSet = new SCOReReadFieldAccess[DEFAULT_CAPACITY];
	private int next = 0;

	public SCOReReadSet()
	{
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

	public synchronized boolean releaseSharedLocks(String txID)
	{ // assumes that these locks are held
		boolean res = true;
		for (int i = 0; i < next; i++)
		{
			if (readSet[i] != null)
			{
				res &= ((InPlaceRWLock) readSet[i].field).sharedUnlock(txID);
			}
		}
		return res;
	}

	public synchronized boolean getSharedLocks(String txID)
	{ // it locks *ALL* the TxFields in the RS (including non local ones)
		boolean res = true;
		int i = 0;
		while (res && i < next)
		{
			if (readSet[i] != null)
			{
				res = ((InPlaceRWLock) readSet[i].field).sharedLock(txID);
			}
			i++;
		}

		if (!res)
		{
			for (int j = 0; j < i - 1; j++)
			{
				if (readSet[j] != null)
				{
					((InPlaceRWLock) readSet[j].field).sharedUnlock(txID);
				}
			}
		}

		return res;
	}

	public synchronized boolean validate(int sid)
	{ // validate *ONLY* the TxFields that are local
		for (int i = 0; i < next; i++)
		{
			if (readSet[i] != null)
			{
				PartialReplicationOID meta = (PartialReplicationOID) readSet[i].field
						.getMetadata();
				if (meta.getPartialGroup().isLocal())
				{
					if (((VBoxField) readSet[i].field).getLastVersion().version > sid)
					{
						return false;
					}
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
					.getMetadata()).getPartialGroup();

			if (!other.isAll())
			{ // never do union with the group ALL
				resGroup = resGroup.union(other);
			}
		}

		return resGroup;
	}

	public String toString()
	{
		StringBuffer sb = new StringBuffer("[");
		for (int i = 0; i < next; i++)
		{
			sb.append(readSet[i] + " ");
		}
		sb.insert(sb.length() - 1, "]");

		return sb.toString().trim();
	}
}
