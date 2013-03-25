package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class CharWriteFieldAccess extends WriteFieldAccess
{
	public char value;

	public void set(char value, VBox field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put(int txNumber)
	{
		((VBoxC) field).commit(value, txNumber);
	}

	public char getValue()
	{
		return value;
	}
}
