package org.deuce.transaction.score;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;
import org.deuce.distribution.replication.partial.oid.PartialReplicationOID;
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
			{ // OPT is this better?
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
		boolean a = writeSet.add(write);
		LOGGER.trace(">>> "+write.field.getMetadata() + " " + a);
		if (!a)
		{
			writeSet.replace(write);
		}
	}

	public void apply(int sid)
	{ // apply only the TxFields that I replicate
		for (SCOReWriteFieldAccess a : writeSet)
		{
			if (TribuDSTM.isLocalGroup(((PartialReplicationOID) a.field
					.getMetadata()).getGroup()))
			{
				a.put(sid);
			}
		}
	}

	public void releaseExclusiveLocks()
	{ // assumes that these locks are held
		for (SCOReWriteFieldAccess a : writeSet)
		{
			try
			{
				((InPlaceRWLock) a.field).exclusiveUnlock();
			}
			catch (IllegalMonitorStateException e)
			{ // lock is not held by this thread
			} // ignore exception
		}
	}

	public boolean getExclusiveLocks()
	{
		boolean res = true;
		int i = 0;
		Object[] ws = writeSet.toArray();

		while (res && i < ws.length)
		{
			res = ((InPlaceRWLock) ((SCOReWriteFieldAccess) ws[i]).field)
					.exclusiveLock();
			i++;
		}

		if (!res)
		{
			for (int j = i - 1; j >= 0; j--)
			{
				try
				{
					((InPlaceRWLock) ((SCOReWriteFieldAccess) ws[j]).field)
							.exclusiveUnlock();
				}
				catch (IllegalMonitorStateException e)
				{ // lock is not held by this thread. THIS SHOULD NOT HAPPEN
					System.err.println("Couldn't unlock all write locks.");
					e.printStackTrace();
					System.exit(-1);
				} // ignore exception
			}
		}
		return res;
	}

	public String toString()
	{
		return writeSet.toString();
	}
}
