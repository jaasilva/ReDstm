package org.deuce.distribution.groupcomm.spread;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class SpreadAddress extends Address
{
	private String addr;

	public SpreadAddress(String addr)
	{
		this.addr = addr;
	}

	@Override
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

	@Override
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
