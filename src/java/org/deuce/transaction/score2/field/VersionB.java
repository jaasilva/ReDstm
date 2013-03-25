package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class VersionB extends Version
{
	public byte value;

	public VersionB(int version, byte value, Version next)
	{
		super(version, next);
		this.value = value;
	}
}
