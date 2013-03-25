package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class ByteWriteFieldAccess extends WriteFieldAccess
{
	public byte value;

	public void set(byte value, VBox field)
	{
		super.init(field);
		this.value = value;
	}

	@Override
	public void put(int txNumber)
	{
		((VBoxB) field).commit(value, txNumber);
	}

	public byte getValue()
	{
		return value;
	}
}