package org.deuce.distribution.cache;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class Validity implements Serializable
{
	private static final long serialVersionUID = 1L;
	public int validity;
	public boolean isShared;

	public Validity(int validity, boolean isShared)
	{
		this.validity = validity;
		this.isShared = isShared;
	}
}
