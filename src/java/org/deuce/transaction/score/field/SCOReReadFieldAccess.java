package org.deuce.transaction.score.field;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReReadFieldAccess implements Serializable
{
	private static final long serialVersionUID = 1L;
	public VBoxField field;

	public SCOReReadFieldAccess()
	{
	}

	public SCOReReadFieldAccess(VBoxField field)
	{
		init(field);
	}

	public void init(VBoxField field)
	{
		this.field = field;
	}

	public boolean equals(Object obj)
	{
		SCOReReadFieldAccess other = (SCOReReadFieldAccess) obj;
		return field == other.field;
	}

	public int hashcode()
	{
		return field.hashCode();
	}

	public void clear()
	{
		field = null;
	}

	public String toString()
	{
		return field.toString();
	}
}
