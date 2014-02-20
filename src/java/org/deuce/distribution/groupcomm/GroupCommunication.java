package org.deuce.distribution.groupcomm;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.deuce.Defaults;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.group.Group;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public abstract class GroupCommunication
{
	protected DeliverySubscriber subscriber = null;

	private final CountDownLatch waitForMembers = new CountDownLatch(1);

	protected Address myAddress;

	public GroupCommunication()
	{
		init();
		System.err.println("-- Waiting for members...");
		try
		{
			waitForMembers.await();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		// defines node rank based in gcs' members list
		System.setProperty(Defaults._SITE,
				"" + (getMembers().indexOf(getLocalAddress()) + 1));

		System.err.println("-- Members arrived!");
	}

	protected void membersArrived()
	{
		waitForMembers.countDown();
	}

	public abstract void init();

	public abstract void close();

	public abstract void sendTotalOrdered(byte[] payload);

	public abstract void sendTotalOrdered(byte[] payload, Group group);

	public abstract void sendReliably(byte[] payload);

	public abstract void sendReliably(byte[] payload, Address addr);

	public abstract void sendReliably(byte[] payload, Group group);

	public abstract List<Address> getMembers();

	public void subscribeDelivery(DeliverySubscriber subscriber)
	{
		this.subscriber = subscriber;
	}

	protected void notifyDelivery(Object obj, Address src, int payloadSize)
	{
		if (subscriber != null)
			subscriber.onDelivery(obj, src, payloadSize);
	}

	public boolean isLocal(Address addr)
	{
		return myAddress.equals(addr);
	}

	public Address getLocalAddress()
	{
		return this.myAddress;
	}
}
