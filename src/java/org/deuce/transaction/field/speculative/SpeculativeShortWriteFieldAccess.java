package org.deuce.transaction.field.speculative;

import org.deuce.transaction.speculative.SpeculativeContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;
import org.deuce.transform.localmetadata.type.speculative.SpeculativeTxField;

@ExcludeTM
public class SpeculativeShortWriteFieldAccess extends
		SpeculativeWriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private short value;

	public void set(short value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	public void put()
	{
		((SpeculativeTxField) field).commitShort(value);
	}

	public void speculativePut(SpeculativeContext ctx)
	{
		((SpeculativeTxField) field).speculativeCommit(value, ctx);
	}

	public void speculativeRemove()
	{
		((SpeculativeTxField) field).speculativeAbort(value);
	}

	public short getValue()
	{
		return value;
	}
}
