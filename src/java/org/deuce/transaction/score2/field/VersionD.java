package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class VersionD extends Version
{
	public double value;

	public VersionD(int version, double value, Version next)
	{
		super(version, next);
		this.value = value;
	}
}
