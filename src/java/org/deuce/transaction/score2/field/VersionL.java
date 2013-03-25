package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class VersionL extends Version
{
	public long value;

	public VersionL(int version, long value, Version next)
	{
		super(version, next);
		this.value = value;
	}
}
