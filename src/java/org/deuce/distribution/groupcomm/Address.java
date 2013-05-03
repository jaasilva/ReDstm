package org.deuce.distribution.groupcomm;

import org.deuce.distribution.TribuDSTM;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public abstract class Address
{
	public boolean isLocal()
	{
		return TribuDSTM.isLocalAddress(this);
	}

	public abstract Object getSpecificAddress();

	@Override
	public abstract String toString();

	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();
}
