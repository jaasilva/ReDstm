package org.deuce.transaction.score.field;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReWriteFieldAccess extends SCOReReadFieldAccess
{
	private static final long serialVersionUID = 1L;
	public Object value;

	public void put(int sid)
	{
		field.commit(value, sid);
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

	public String toString()
	{
		return field.getMetadata().toString();
	}
}
