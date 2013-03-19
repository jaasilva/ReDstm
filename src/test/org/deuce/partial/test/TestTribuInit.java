package org.deuce.partial.test;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;

public class TestTribuInit
{
	public static void main(String[] args)
	{
		TribuDSTM.init();
		TribuDSTM.subscribeDeliveries(new Receiver());

		

		
	}
}

class Receiver implements DeliverySubscriber
{
	@Override
	public void onDelivery(Object obj, Address src, int size)
	{
		System.out.println(obj);
	}
}
