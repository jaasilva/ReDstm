package org.deuce.distribution.groupcomm;

import org.deuce.distribution.TribuDSTM;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public abstract class Address {
	@Override
	public abstract boolean equals(Object obj);	
	
	public boolean isLocal() {
		return TribuDSTM.isLocalAddress(this);
	}
	
	@Override
	public abstract String toString();
}
