package org.deuce.distribution.groupcomm.jgroups;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class JGroupsAddress extends Address
{
	private org.jgroups.Address addr;

	public JGroupsAddress(org.jgroups.Address addr)
	{
		this.addr = addr;
	}

	public boolean equals(Object obj)
	{
		if (obj instanceof JGroupsAddress)
		{
			JGroupsAddress other = (JGroupsAddress) obj;
			return this.addr.compareTo(other.addr) == 0;
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
