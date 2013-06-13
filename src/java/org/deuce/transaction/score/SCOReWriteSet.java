package org.deuce.transaction.score;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;
import org.deuce.distribution.replication.partial.oid.PartialReplicationOID;
import org.deuce.transaction.score.field.InPlaceRWLock;
import org.deuce.transaction.score.field.SCOReReadFieldAccess;
import org.deuce.transaction.score.field.SCOReWriteFieldAccess;
import org.deuce.transform.ExcludeTM;
import org.deuce.trove.THashSet;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReWriteSet implements Serializable
{
	private static final Logger LOGGER = Logger.getLogger(SCOReWriteSet.class);
	private static final long serialVersionUID = 1L;
	private final THashSet<SCOReWriteFieldAccess> writeSet = new THashSet<SCOReWriteFieldAccess>(
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
		Group resGroup = new PartialReplicationGroup();

		for (SCOReWriteFieldAccess wfa : writeSet)
		{
			Group other = ((PartialReplicationOID) wfa.field.getMetadata())
					.getGroup();

			if (TribuDSTM.groupIsAll(other))
			{ // OPT is this better? vale a pena este verificação sempre??
				return other;
			}

			resGroup = resGroup.union(other);
		}

		return resGroup;
	}

	public SCOReWriteFieldAccess contains(SCOReReadFieldAccess read)
	{ // check if it is already included in the write set
		return writeSet.get(read);
	}

	public void put(SCOReWriteFieldAccess write)
	{ // add to write set
		if (!writeSet.add(write))
		{
			writeSet.replace(write);
		}
	}

	public synchronized void apply(int sid)
	{ // apply only the TxFields that I replicate
		for (SCOReWriteFieldAccess a : writeSet)
		{
			if (TribuDSTM.isLocalGroup(((PartialReplicationOID) a.field
					.getMetadata()).getGroup()))
			{
				a.put(sid);

				try
				{
					LOGGER.trace("APPLY " + a.field.getMetadata() + " -> "
							+ a.value + " (sid=" + sid + ")");
				}
				catch (NullPointerException e)
				{
					LOGGER.trace("APPLY " + a.field.getMetadata()
							+ " -> NullPtr (sid=" + sid + ")");
				}
			}
		}
	}

	public synchronized boolean releaseExclusiveLocks(String txID)
	{ // assumes that these locks are held
		boolean res = false;
		for (SCOReWriteFieldAccess a : writeSet)
		{
			res = ((InPlaceRWLock) a.field).exclusiveUnlock(txID);
			LOGGER.trace("XunLock " + a.field.getMetadata() + " " + res);
		}
		return res;
	}

	public synchronized boolean getExclusiveLocks(String txID)
	{
		boolean res = true;
		int i = 0;
		Object[] ws = writeSet.toArray();
		while (res && i < ws.length)
		{
			res = ((InPlaceRWLock) ((SCOReWriteFieldAccess) ws[i]).field)
					.exclusiveLock(txID);
			LOGGER.trace("XLock "
					+ ((SCOReWriteFieldAccess) ws[i]).field.getMetadata() + " "
					+ res);
			i++;
		}

		if (!res)
		{
			if (i > 1)
			{ // there is only 1 elem in WS. it is not locked
				for (int j = i - 2; j >= 0; j--)
				{
					res = ((InPlaceRWLock) ((SCOReWriteFieldAccess) ws[j]).field)
							.exclusiveUnlock(txID);
					LOGGER.trace("-> XunLock "
							+ ((SCOReWriteFieldAccess) ws[j]).field
									.getMetadata() + " " + res);
				}
			}
		}

		return res;
	}

	public String toString()
	{
		return writeSet.toString();
	}
}
