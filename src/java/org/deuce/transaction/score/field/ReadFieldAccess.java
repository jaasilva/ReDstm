package org.deuce.transaction.score.field;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class ReadFieldAccess implements Serializable
{
	private static final long serialVersionUID = 1L;
	public VBoxField field; // CHECKME devia ser VBoxField?
	public Version version;

	public ReadFieldAccess()
	{
	}

	public ReadFieldAccess(VBoxField field)
	{
		init(field);
	}

	public void init(VBoxField field)
	{
		init(field, null);
	}

	public void init(VBoxField field, Version version)
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
