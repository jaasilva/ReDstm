package org.deuce.transaction.field;

import org.deuce.transaction.SpeculativeContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;
import org.deuce.transform.localmetadata.type.speculative.SpeculativeTxField;

@ExcludeTM
public class SpeculativeDoubleWriteFieldAccess extends
		SpeculativeWriteFieldAccess
{
	private static final long serialVersionUID = 1L;
	private Double value;

	public void set(double value, TxField field)
	{
		super.init(field);
		this.value = value;
	}

	public void put()
	{
		((SpeculativeTxField) field).commitDouble(value);
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

	public Double getValue()
	{
		return value;
	}
}
