package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class FloatWriteFieldAccess extends WriteFieldAccess
{
	public float value;

	public void set(float value, VBox field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put(int txNumber)
	{
		((VBoxF) field).commit(value, txNumber);
	}

	public float getValue()
	{
		return value;
	}
}
