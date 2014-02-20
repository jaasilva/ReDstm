package org.deuce.transaction.mvstm.field;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * Represents a base class for field write access.
 * 
 * @author Guy Korland, Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class WriteFieldAccess extends ReadFieldAccess
{
	private static final long serialVersionUID = 1L;
	public Object value;

	public void put(int txNumber)
	{
		((VBoxField) field).commit(value, txNumber);
	}

	public void set(Object value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	public Object getValue()
	{
		return value;
	}

	@Override
	public String toString()
	{
		return field.getMetadata().toString();
	}
}
