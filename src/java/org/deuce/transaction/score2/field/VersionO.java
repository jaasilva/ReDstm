package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class VersionO extends Version
{
	public Object value;

	public VersionO(int version, Object value, Version next)
	{
		super(version, next);
		this.value = value;
	}
}
