package org.deuce.transaction.score.field;

import org.deuce.transaction.score.field.VBoxField.__Type;
import org.deuce.transform.ExcludeTM;

/**
 * Represents a base class for field write access.
 * 
 * @author Guy Korland, Ricardo Dias <ricardo.dias@campus.fct.unl.pt>, jaasilva
 */
@ExcludeTM
public class WriteFieldAccess extends ReadFieldAccess
{
	private static final long serialVersionUID = 1L;
	public Object value;
	public __Type type;

	/**
	 * Commits the value in memory.
	 */
	public void put(int txNumber)
	{
		((VBoxField) field).commit(value, txNumber);
	}

	public void set(Object value, VBoxField field, __Type type)
	{
		super.init(field);
		this.value = value;
		this.type = type;
	}

	public Object getValue()
	{
		return value;
	}
}
