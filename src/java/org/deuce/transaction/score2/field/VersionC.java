package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class VersionC extends Version
{
	public char value;

	public VersionC(int version, char value, Version next)
	{
		super(version, next);
		this.value = value;
	}
}
