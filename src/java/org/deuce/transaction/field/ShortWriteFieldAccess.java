package org.deuce.transaction.field;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class ShortWriteFieldAccess extends WriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private short value;

	public void set(short value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put()
	{
		field.writeShort(value);
	}

	public Short getValue()
	{
		return value;
	}
}
