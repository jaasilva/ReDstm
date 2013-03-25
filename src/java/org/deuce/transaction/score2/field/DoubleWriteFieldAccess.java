package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class DoubleWriteFieldAccess extends WriteFieldAccess
{
	public double value;

	public void set(double value, VBox field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put(int txNumber)
	{
		((VBoxD) field).commit(value, txNumber);
	}

	public double getValue()
	{
		return value;
	}
}
