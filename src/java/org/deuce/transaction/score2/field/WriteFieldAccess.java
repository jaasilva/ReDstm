package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * Represents a base class for field write access.
 * 
 * @author Guy Korland, Ricardo Dias <ricardo.dias@campus.fct.unl.pt>, jaasilva
 */
@ExcludeTM
public class WriteFieldAccess extends ReadFieldAccess
{
	public Object value;

	/**
	 * Commits the value in memory.
	 */
	public void put(int txNumber)
	{
		field.commit(value, txNumber);
	}

	public void set(Object value, VBoxField field)
	{
		super.init(field);
		this.value = value;
	}

	public Object getValue()
	{
		return value;
	}
}
