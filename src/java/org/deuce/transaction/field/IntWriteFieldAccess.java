package org.deuce.transaction.field;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class IntWriteFieldAccess extends WriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private int value;

	public void set(int value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put()
	{
		field.writeInt(value);
	}

	public int getValue()
	{
		return value;
	}
}
