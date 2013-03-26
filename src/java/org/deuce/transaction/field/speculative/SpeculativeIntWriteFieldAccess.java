package org.deuce.transaction.field.speculative;

import org.deuce.transaction.SpeculativeContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;
import org.deuce.transform.localmetadata.type.speculative.SpeculativeTxField;

@ExcludeTM
public class SpeculativeIntWriteFieldAccess extends SpeculativeWriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private Integer value;

	public void set(int value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	public void put()
	{
		((SpeculativeTxField) field).commitInt(value);
		// clear();
		// value = null;
	}

	public void speculativePut(SpeculativeContext ctx)
	{
		((SpeculativeTxField) field).speculativeCommit(value, ctx);
	}

	public void speculativeRemove()
	{
		((SpeculativeTxField) field).speculativeAbort(value);
	}

	public Integer getValue()
	{
		return value;
	}
}
