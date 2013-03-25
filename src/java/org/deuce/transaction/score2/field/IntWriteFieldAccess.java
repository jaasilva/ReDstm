package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class IntWriteFieldAccess extends WriteFieldAccess
{
	public int value;

	public void set(int value, VBox field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put(int txNumber)
	{
		((VBoxI) field).commit(value, txNumber);
	}

	public int getValue()
	{
		return value;
	}
}
