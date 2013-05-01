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
		// UnsafeHolder.getUnsafe().putShort(field.ref, field.address, value);
		field.writeShort(value);
		// clear();
	}

	public short getValue()
	{
		return value;
	}

}
