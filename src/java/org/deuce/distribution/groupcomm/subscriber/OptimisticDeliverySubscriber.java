package org.deuce.distribution.groupcomm.subscriber;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public interface OptimisticDeliverySubscriber extends DeliverySubscriber
{
	public Object onOptimisticDelivery(Object obj, Address src, int payloadSize);
}
