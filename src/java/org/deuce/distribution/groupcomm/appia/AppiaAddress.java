package org.deuce.distribution.groupcomm.appia;

import java.net.SocketAddress;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class AppiaAddress extends Address
{
	private SocketAddress addr;

	public AppiaAddress(SocketAddress addr)
	{
		this.addr = addr;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof AppiaAddress)
		{
			AppiaAddress other = (AppiaAddress) obj;
			return this.addr.equals(other.addr);
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
