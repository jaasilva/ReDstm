package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class BooleanWriteFieldAccess extends WriteFieldAccess
{
	public boolean value;

	public void set(boolean value, VBox field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put(int txNumber)
	{
		((VBoxZ) field).commit(value, txNumber);
	}

	public boolean getValue()
	{
		return value;
	}
}
