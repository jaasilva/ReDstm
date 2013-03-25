package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class VersionI extends Version
{
	public int value;

	public VersionI(int version, int value, Version next)
	{
		super(version, next);
		this.value = value;
	}
}