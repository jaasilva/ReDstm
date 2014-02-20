package org.deuce.transaction.score.field;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 */
@ExcludeTM
public class WriteFieldAccess extends ReadFieldAccess
{
	private static final long serialVersionUID = 1L;
	public Object value;

	public void put(int sid)
	{
		((VBoxField) field).commit(value, sid);
	}

	public void set(Object value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	public Object getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return field.getMetadata().toString();
	}
}
