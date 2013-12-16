package org.deuce.transaction.mvstm.field;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class ReadFieldAccess implements Serializable
{
	private static final long serialVersionUID = 1L;
	public TxField field;
	public Version version;

	public ReadFieldAccess()
	{
	}

	public ReadFieldAccess(TxField field)
	{
		init(field);
	}

	public void init(TxField field)
	{
		init(field, null);
	}

	public void init(TxField field, Version version)
	{
		this.field = field;
		this.version = version;
	}

	@Override
	public boolean equals(Object obj)
	{
		ReadFieldAccess other = (ReadFieldAccess) obj;
		return field == other.field;
	}

	@Override
	final public int hashCode()
	{
		return field.hashCode();
	}

	public void clear()
	{
		field = null;
		version = null;
	}
}
