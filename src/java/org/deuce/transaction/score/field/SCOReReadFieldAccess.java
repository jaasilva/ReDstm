package org.deuce.transaction.score.field;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReReadFieldAccess implements Serializable
{
	private static final long serialVersionUID = 1L;
	public TxField field;

	public SCOReReadFieldAccess()
	{
	}

	public SCOReReadFieldAccess(TxField field)
	{
		init(field);
	}

	public void init(TxField field)
	{
		this.field = field;
	}

	public boolean equals(Object obj)
	{
		SCOReReadFieldAccess other = (SCOReReadFieldAccess) obj;
		return field == other.field;
	}

	public int hashCode()
	{
		return field.hashCode();
	}

	public void clear()
	{
		field = null;
	}

	public String toString()
	{
		return field.getMetadata().toString();
	}
}
