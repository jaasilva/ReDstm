package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class VersionS extends Version
{
	public short value;

	public VersionS(int version, short value, Version next)
	{
		super(version, next);
		this.value = value;
	}
}
