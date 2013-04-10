package org.deuce.transform.localmetadata.type.speculative;

import org.deuce.transaction.speculative.SpeculativeContext;

public class SpeculativeVersion
{
	final public Object value;
	final public SpeculativeContext ctx;

	public SpeculativeVersion(Object value, SpeculativeContext ctx)
	{
		this.value = value;
		this.ctx = ctx;
	}

	@Override
	public boolean equals(Object obj)
	{
		return value.equals(obj);
	}

	@Override
	public int hashCode()
	{
		return value.hashCode();
	}
}
