package org.deuce.transaction.score.field;

import org.deuce.transaction.field.ReadFieldAccess;
import org.deuce.transaction.score.Version;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 * 
 */
public class SCOReReadFieldAccess extends ReadFieldAccess
{
	private static final long serialVersionUID = 8887432083237434368L;
	private Version version;

	/**
	 * 
	 */
	public SCOReReadFieldAccess()
	{
	}

	/**
	 * @param field
	 */
	public SCOReReadFieldAccess(TxField field)
	{
		init(field);
	}

	@Override
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
	public void clear()
	{
		field = null;
		version = null;
	}
}
