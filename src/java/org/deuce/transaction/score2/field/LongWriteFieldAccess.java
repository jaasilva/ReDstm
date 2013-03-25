package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class LongWriteFieldAccess extends WriteFieldAccess
{
	public long value;

	public void set(long value, VBox field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put(int txNumber)
	{
		((VBoxL) field).commit(value, txNumber);
	}

	public long getValue()
	{
		return value;
	}
}
