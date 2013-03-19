package org.deuce.partial.test;

import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;

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
