package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class VersionF extends Version
{
	public float value;

	public VersionF(int version, float value, Version next)
	{
		super(version, next);
		this.value = value;
	}
}
