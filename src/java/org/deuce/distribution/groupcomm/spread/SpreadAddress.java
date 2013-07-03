package org.deuce.distribution.groupcomm.spread;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class SpreadAddress extends Address
{
	// private SpreadGroup addr;
	private String addr;

	public SpreadAddress(/* SpreadGroup */String addr)
	{
		this.addr = addr;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof SpreadAddress)
		{
			SpreadAddress other = (SpreadAddress) obj;
			return this.addr.equals(other.addr);
		}
		else
		{
			return false;
		}
	}

	public String toString()
	{
		return this.addr.toString();
	}

	@Override
	public Object getSpecificAddress()
	{
		return this.addr;
	}

	@Override
	public int hashCode()
	{
		return this.addr.hashCode();
	}
}
