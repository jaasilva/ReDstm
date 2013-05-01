package org.deuce.transaction.field;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ArrayContainer;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class ArrayWriteFieldAccess extends WriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private ArrayContainer value;

	public void set(ArrayContainer value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put()
	{
		// UnsafeHolder.getUnsafe().putObject(field.ref, field.address, value);
		field.writeArray(value);
		// clear();
		value = null;
	}

	public ArrayContainer getValue()
	{
		return value;
	}
}
