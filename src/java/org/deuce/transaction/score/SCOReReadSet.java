package org.deuce.transaction.score;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;
import org.deuce.distribution.replication.partial.oid.PartialReplicationOID;
import org.deuce.transaction.score.field.InPlaceRWLock;
import org.deuce.transaction.score.field.SCOReReadFieldAccess;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReReadSet implements Serializable
{
	private static final Logger LOGGER = Logger.getLogger(SCOReReadSet.class);
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
		boolean res = false;
		for (int i = 0; i < next; i++)
		{
			res = ((InPlaceRWLock) readSet[i].field).sharedUnlock(txID);
			LOGGER.trace("SunLock " + readSet[i].field.getMetadata() + " "
					+ res);
		}

		return res;
	}

	public synchronized boolean getSharedLocks(String txID)
	{
		boolean res = true;
		int i = 0;
		while (res && i < next)
		{
			res = ((InPlaceRWLock) readSet[i].field).sharedLock(txID);
			LOGGER.trace("SLock " + readSet[i].field.getMetadata() + " " + res);
			i++;
		}

		if (!res)
		{
			for (int j = 0; j < i - 1; j++)
			{
				boolean unlock_res = ((InPlaceRWLock) readSet[j].field).sharedUnlock(txID);
				LOGGER.trace("-> SunLock " + readSet[j].field.getMetadata()
						+ " " + unlock_res);
			}
		}

		return res;
	}

	public synchronized boolean validate(int sid)
	{ // validate only the TxFields that I replicate
		for (int i = 0; i < next; i++)
		{
			if (TribuDSTM
					.isLocalGroup(((PartialReplicationOID) readSet[i].field
							.getMetadata()).getPartialGroup()))
			{
				boolean res = ((VBoxField) readSet[i].field).getLastVersion().version > sid;
				LOGGER.trace("VAL "
						+ ((VBoxField) readSet[i].field).getMetadata()
						+ " "
						+ ((VBoxField) readSet[i].field).getLastVersion().version
						+ " " + sid + " -> " + res);
				if (res)
				{
					return false;
				}
			}
		}

		return true;
	}

	public Group getInvolvedNodes()
	{
		Group resGroup = new PartialReplicationGroup(-2);

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
		StringBuffer sb = new StringBuffer("[");
		for (int i = 0; i < next; i++)
		{
			sb.append(readSet[i] + " ");
		}
		sb.insert(sb.length() - 1, "]");

		return sb.toString().trim();
	}
}
