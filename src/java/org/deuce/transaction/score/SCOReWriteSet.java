package org.deuce.transaction.score;

import java.io.Serializable;

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
 */
@ExcludeTM
public class SCOReWriteSet implements Serializable
{
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
					.getPartialGroup();
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
	{ // apply *ONLY* the TxFields that I replicate
		for (SCOReWriteFieldAccess a : writeSet)
		{
			PartialReplicationOID meta = ((PartialReplicationOID) a.field
					.getMetadata());
			if (meta.getPartialGroup().isLocal())
			{
				a.put(sid);
			}
		}
	}

	public synchronized boolean releaseExclusiveLocks(String txID)
	{ // assumes that these locks are held
		boolean res = true;
		for (SCOReWriteFieldAccess a : writeSet)
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
			res = ((InPlaceRWLock) ((SCOReWriteFieldAccess) ws[i]).field)
					.exclusiveLock(txID);
			i++;
		}

		if (!res)
		{
			for (int j = 0; j < i - 1; j++)
			{
				((InPlaceRWLock) ((SCOReWriteFieldAccess) ws[j]).field)
						.exclusiveUnlock(txID);
			}
		}

		return res;
	}

	public String toString()
	{
		return writeSet.toString();
	}
}
