package org.deuce.distribution.groupcomm.spread;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

import spread.SpreadGroup;

@ExcludeTM
public class SpreadAddress extends Address
{
	private SpreadGroup addr;

	public SpreadAddress(SpreadGroup addr)
	{
		this.addr = addr;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof SpreadAddress)
		{
			SpreadAddress other = (SpreadAddress) obj;
			return addr.equals(other.addr);
		}
		else
		{
			return false;
		}
	}

	public String toString()
	{
		return addr.toString();
	}

	public Object getSpecificAddress()
	{
		return addr;
	}

}
