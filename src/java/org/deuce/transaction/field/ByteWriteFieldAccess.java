package org.deuce.transaction.field;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class ByteWriteFieldAccess extends WriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private byte value;

	public void set(byte value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put()
	{
		// UnsafeHolder.getUnsafe().putByte(field.ref, field.address, value);
		field.writeByte(value);
		// clear();
	}

	public byte getValue()
	{
		return value;
	}
}
