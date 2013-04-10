package org.deuce.transaction.field;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class DoubleWriteFieldAccess extends WriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private double value;

	public void set(double value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put()
	{
		field.writeDouble(value);
	}

	public double getValue()
	{
		return value;
	}
}
