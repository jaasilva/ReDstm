package org.deuce.distribution.groupcomm.subscriber;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public interface DeliverySubscriber {
	public void onDelivery(Object obj, Address src, int size);
}
