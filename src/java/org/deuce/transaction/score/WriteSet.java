package org.deuce.transaction.score;

import java.io.Serializable;
import java.util.Set;

import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;
import org.deuce.distribution.replication.partial.PartialReplicationOID;
import org.deuce.transaction.score.field.InPlaceRWLock;
import org.deuce.transaction.score.field.ReadFieldAccess;
import org.deuce.transaction.score.field.WriteFieldAccess;
import org.deuce.transform.ExcludeTM;
import org.deuce.trove.THashSet;
import org.deuce.trove.TObjectProcedure;

/**
 * @author jaasilva
 */
@ExcludeTM
public class WriteSet implements Serializable
{
	private static final long serialVersionUID = 1L;
	private final THashSet<WriteFieldAccess> writeSet = new THashSet<WriteFieldAccess>(
			16);

	public Set<WriteFieldAccess> getWrites()
	{
		return writeSet;
	}

	public void clear()
	{
		writeSet.clear();
	}

	public boolean isEmpty()
	{
		return writeSet.isEmpty();
	}

	public Group getInvolvedNodes()
	{ // CHECKME
		Group resGroup = new PartialReplicationGroup();

		for (WriteFieldAccess wfa : writeSet)
		{
			Group other = ((PartialReplicationOID) wfa.field.getMetadata())
					.getPartialGroup();
			resGroup = resGroup.union(other);
		}

		return resGroup;
	}

	public WriteFieldAccess contains(ReadFieldAccess read)
	{ // check if it is already included in the write set
		return writeSet.get(read);
	}

	public void put(WriteFieldAccess write)
	{ // add to write set
		if (!writeSet.add(write))
		{
			writeSet.replace(write);
		}
	}

	public boolean forEach(TObjectProcedure<WriteFieldAccess> procedure)
	{
		return writeSet.forEach(procedure);
	}

	public synchronized boolean releaseExclusiveLocks(String txID)
	{ // assumes that these locks are held
		boolean res = true;
		for (WriteFieldAccess a : writeSet)
		{
			res &= ((InPlaceRWLock) a.field).exclusiveUnlock(txID);
		}
		return res;
	}

	public synchronized boolean getExclusiveLocks(String txID)
	{ // it locks *ALL* the TxFields in the WS (including non local ones)
		boolean res = true;
		int i = 0;
		Object[] ws = writeSet.toArray();
		while (res && i < ws.length)
		{
			res = ((InPlaceRWLock) ((WriteFieldAccess) ws[i]).field)
					.exclusiveLock(txID);
			i++;
		}

		if (!res)
		{
			for (int j = 0; j < i - 1; j++)
			{
				((InPlaceRWLock) ((WriteFieldAccess) ws[j]).field)
						.exclusiveUnlock(txID);
			}
		}

		return res;
	}
}
