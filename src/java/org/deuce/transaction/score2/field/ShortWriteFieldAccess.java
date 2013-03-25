package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class ShortWriteFieldAccess extends WriteFieldAccess
{
	public short value;

	public void set(short value, VBox field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put(int txNumber)
	{
		((VBoxS) field).commit(value, txNumber);
	}

	public short getValue()
	{
		return value;
	}
}