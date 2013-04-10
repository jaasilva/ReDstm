package org.deuce.transaction.field.speculative;

import org.deuce.transaction.speculative.SpeculativeContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;
import org.deuce.transform.localmetadata.type.speculative.SpeculativeTxField;

@ExcludeTM
public class SpeculativeBooleanWriteFieldAccess extends
		SpeculativeWriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private boolean value;

	public void set(boolean value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	public void put()
	{
		((SpeculativeTxField) field).commitBoolean(value);
	}

	public void speculativePut(SpeculativeContext ctx)
	{
		((SpeculativeTxField) field).speculativeCommit(value, ctx);
	}

	public void speculativeRemove()
	{
		((SpeculativeTxField) field).speculativeAbort(value);
	}

	public boolean getValue()
	{
		return value;
	}
}
