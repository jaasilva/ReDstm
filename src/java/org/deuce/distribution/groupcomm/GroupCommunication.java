package org.deuce.distribution.groupcomm;

import java.util.concurrent.CountDownLatch;

import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.groupcomm.subscriber.OptimisticDeliverySubscriber;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public abstract class GroupCommunication
{
	protected DeliverySubscriber subscriber = null;
	protected OptimisticDeliverySubscriber optSubscriber = null;

	private final CountDownLatch waitForMembers = new CountDownLatch(1);

	protected Address myAddress;

	public GroupCommunication()
	{
		init();
		System.out.println("-- Waiting for members...");
		try
		{
			waitForMembers.await();
		}
		catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("-- Members arrived!");
	}

	protected void membersArrived()
	{
		waitForMembers.countDown();
	}

	public abstract void init();

	public abstract void close();

	public abstract void sendTotalOrdered(byte[] payload);

	public abstract void sendReliably(byte[] payload);

	public abstract void sendTo(byte[] payload, Address addr);

	// TODO ver se é necessario
	// ....................................................

	public void subscribeDelivery(DeliverySubscriber subscriber)
	{
		this.subscriber = subscriber;
	}

	public void subscribeOptimisticDelivery(
			OptimisticDeliverySubscriber optSubscriber)
	{
		this.optSubscriber = optSubscriber;
		this.subscriber = optSubscriber;
	}

	protected void notifyDelivery(Object obj, Address src, int payloadSize)
	{
		if (subscriber != null)
			subscriber.onDelivery(obj, src, payloadSize);
	}

	protected Object notifyOptimisticDelivery(Object obj, Address src,
			int payloadSize)
	{

		Object appObj = null;
		if (optSubscriber != null)
			appObj = optSubscriber.onOptimisticDelivery(obj, src, payloadSize);

		return appObj != null ? appObj : obj;
	}

	public boolean isLocal(Address addr)
	{
		return myAddress.equals(addr);
	}
}
