package org.deuce.transaction.tl2.field;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class BooleanWriteFieldAccess extends WriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private boolean value;

	public void set(boolean value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put()
	{
		field.writeBoolean(value);
	}

	public boolean getValue()
	{
		return value;
	}
}
