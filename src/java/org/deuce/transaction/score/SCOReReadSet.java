package org.deuce.transaction.score;

import java.io.Serializable;

import org.apache.log4j.Logger;
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
	private static final Logger LOGGER = Logger.getLogger(SCOReReadSet.class);
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
			LOGGER.debug("@@1 " + readSet[i].field.getMetadata());
			try
			{
				((InPlaceRWLock) readSet[i].field).sharedUnlock();
			}
			catch (IllegalMonitorStateException e)
			{ // lock is not held by this thread
				LOGGER.debug("@@2 " + readSet[i].field.getMetadata());
			} // ignore exception

			LOGGER.debug("@@@@ " + readSet[i].field.getMetadata());
		}
	}

	public boolean getSharedLocks()
	{
		boolean res = true;
		int i = 0;
		while (res && i < next)
		{
			LOGGER.debug("££1 " + readSet[i].field.getMetadata());
			res = ((InPlaceRWLock) readSet[i].field).sharedLock();
			LOGGER.debug("££££2 " + readSet[i].field.getMetadata() + " " + res);
			i++;
		}

		LOGGER.debug("---£££££££££££££££££££££££3 ");

		if (!res)
		{
			LOGGER.debug("---£££££££££££££££££££££££3.1 ");
			
			if (i > 1)
			{ // there is only 1 elem in RS. it is not locked
				LOGGER.debug("---£££££££££££££££££££££££3.1.1 ");
				
				for (int j = i - 1; j >= 0; j--)
				{
					((InPlaceRWLock) readSet[j].field).sharedUnlock();
				}
			}
		}

		LOGGER.debug("---£££££££££££££££££££££££4 ");

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
		StringBuffer sb = new StringBuffer("[");
		for (int i = 0; i < next; i++)
		{
			sb.append(readSet[i] + " ");
		}
		sb.insert(sb.length() - 1, "]");

		return sb.toString().trim();
	}
}
