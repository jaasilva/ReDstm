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
		boolean a = writeSet.add(write);
		LOGGER.trace(">>> " + write.field.getMetadata() + " " + a);
		if (!a)
		{
			writeSet.replace(write);
		}
	}

	public void apply(int sid)
	{ // apply only the TxFields that I replicate
		for (SCOReWriteFieldAccess a : writeSet)
		{
			LOGGER.debug("&& " + a.field.getMetadata());
			if (TribuDSTM.isLocalGroup(((PartialReplicationOID) a.field
					.getMetadata()).getGroup()))
			{
				a.put(sid);
				LOGGER.debug("&&&& " + a.field.getMetadata());
			}
		}
	}

	public void releaseExclusiveLocks()
	{ // assumes that these locks are held
		for (SCOReWriteFieldAccess a : writeSet)
		{
			LOGGER.debug("%% " + a.field.getMetadata());
			try
			{
				((InPlaceRWLock) a.field).exclusiveUnlock();
				LOGGER.debug("%%%% " + a.field.getMetadata());
			}
			catch (IllegalMonitorStateException e)
			{ // lock is not held by this thread
				LOGGER.debug("%%2 " + a.field.getMetadata());
			} // ignore exception

			LOGGER.debug("%%3 " + a.field.getMetadata());
		}
	}

	public boolean getExclusiveLocks()
	{
		boolean res = true;
		int i = 0;
		Object[] ws = writeSet.toArray();

		while (i < ws.length && res)
		{
			LOGGER.debug("__1 "
					+ ((SCOReWriteFieldAccess) ws[i]).field.getMetadata() + " "
					+ ws.length);
			res = ((InPlaceRWLock) ((SCOReWriteFieldAccess) ws[i]).field)
					.exclusiveLock();
			i++;

			LOGGER.debug("____2 "
					+ ((SCOReWriteFieldAccess) ws[i - 1]).field.getMetadata()
					+ " " + res + " " + (i < ws.length));
		}

		LOGGER.debug("___________________________3 ");

		if (!res)
		{
			LOGGER.debug("___________________________3.1 ");

			if (i > 1)
			{ // there is only 1 elem in WS. it is not locked
				LOGGER.debug("___________________________3.1.1 ");

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
		}

		LOGGER.debug("___________________________4 ");

		return res;
	}

	public String toString()
	{
		return writeSet.toString();
	}
}
