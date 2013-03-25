package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class VersionZ extends Version
{
	public boolean value;

	public VersionZ(int version, boolean value, Version next)
	{
		super(version, next);
		this.value = value;
	}
}
