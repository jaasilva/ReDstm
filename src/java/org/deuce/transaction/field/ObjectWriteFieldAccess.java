package org.deuce.transaction.field;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class ObjectWriteFieldAccess extends WriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private Object value;

	public void set(Object value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put()
	{
		field.writeObject(value);
		value = null;
	}

	public Object getValue()
	{
		return value;
	}
}
