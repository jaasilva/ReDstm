package org.deuce.transaction.score;

import org.deuce.distribution.replication.group.Group;
import org.deuce.transaction.WriteSet;
import org.deuce.transaction.score.field.ReadFieldAccess;
import org.deuce.transaction.score.field.WriteFieldAccess;
import org.deuce.transform.ExcludeTM;
import org.deuce.trove.THashSet;
import org.deuce.trove.TObjectProcedure;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReWriteSet extends WriteSet
{
	private static final long serialVersionUID = 3147866584383152424L;
	final private THashSet<WriteFieldAccess> writeSet = new THashSet<WriteFieldAccess>(
			16);

	public void clear()
	{
		writeSet.clear();
	}

	public boolean isEmpty()
	{
		return writeSet.isEmpty();
	}

	public boolean scoreForEach(TObjectProcedure<WriteFieldAccess> procedure)
	{
		return writeSet.forEach(procedure);
	}

	public void put(WriteFieldAccess write)
	{ // Add to write set
		if (!writeSet.add(write))
			writeSet.replace(write);
	}

	public WriteFieldAccess contains(ReadFieldAccess read)
	{ // Check if it is already included in the write set
		return writeSet.get(read);
	}

	public int size()
	{
		return writeSet.size();
	}

	public Group getInvolvedNodes()
	{
		// TODO Auto-generated constructor stub
		return null;
	}
}
