package org.deuce.transaction.field;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class CharWriteFieldAccess extends WriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private char value;

	public void set(char value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put()
	{
		// UnsafeHolder.getUnsafe().putChar(field.ref, field.address, value);
		field.writeChar(value);
		// clear();
	}

	public char getValue()
	{
		return value;
	}

}
