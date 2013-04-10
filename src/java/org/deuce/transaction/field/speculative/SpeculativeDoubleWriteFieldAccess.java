package org.deuce.transaction.field.speculative;

import org.deuce.transaction.speculative.SpeculativeContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;
import org.deuce.transform.localmetadata.type.speculative.SpeculativeTxField;

@ExcludeTM
public class SpeculativeDoubleWriteFieldAccess extends
		SpeculativeWriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private double value;

	public void set(double value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	public void put()
	{
		((SpeculativeTxField) field).commitDouble(value);
	}

	public void speculativePut(SpeculativeContext ctx)
	{
		((SpeculativeTxField) field).speculativeCommit(value, ctx);
	}

	public void speculativeRemove()
	{
		((SpeculativeTxField) field).speculativeAbort(value);
	}

	public double getValue()
	{
		return value;
	}
}
