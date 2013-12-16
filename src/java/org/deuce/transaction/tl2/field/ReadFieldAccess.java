package org.deuce.transaction.tl2.field;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * Represents a base class for field write access.
 * 
 * @author Guy Koralnd
 */
@ExcludeTM
public class ReadFieldAccess implements java.io.Serializable
{
	private static final long serialVersionUID = 5999603310055045915L;
	public TxField field;

	public ReadFieldAccess()
	{
	}

	public ReadFieldAccess(TxField field)
	{
		init(field);
	}

	public void init(TxField field)
	{
		this.field = field;
	}

	@Override
	public boolean equals(Object obj)
	{
		ReadFieldAccess other = (ReadFieldAccess) obj;
		return field == other.field;
	}

	@Override
	final public int hashCode()
	{
		return field.hashCode();
	}

	public void clear()
	{
		field = null;
	}
}
