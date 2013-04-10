package org.deuce.transaction.field;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class LongWriteFieldAccess extends WriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private long value;

	public void set(long value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put()
	{
		field.writeLong(value);
	}

	public long getValue()
	{
		return value;
	}
}
