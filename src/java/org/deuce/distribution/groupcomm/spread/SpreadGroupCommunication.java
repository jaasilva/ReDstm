package org.deuce.distribution.groupcomm.spread;

import java.util.List;

import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.GroupCommunication;
import org.deuce.distribution.groupcomm.OptimisticDeliveryUnsupportedException;
import org.deuce.distribution.groupcomm.subscriber.OptimisticDeliverySubscriber;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.serialization.GCPayloadException;
import org.deuce.transform.ExcludeTM;

import spread.AdvancedMessageListener;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

@ExcludeTM
public class SpreadGroupCommunication extends GroupCommunication implements
		AdvancedMessageListener
{
	private SpreadConnection connection;
	private SpreadGroup group;
	private SpreadGroup[] members;

	public SpreadGroupCommunication()
	{
		super();
	}

	public void init()
	{
		connection = new SpreadConnection();
		group = new SpreadGroup();
		try
		{
			connection.connect(null, 0,
					"replica" + Integer.getInteger("tribu.site"), false, true);
			connection.add(this);
			group.join(connection, System.getProperty(
					"tribu.groupcommunication.group", "tvale"));
			myAddress = new SpreadAddress(connection.getPrivateGroup());
		}
		catch (SpreadException e)
		{
			System.err.println("Couldn't initialise Spread client.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void close()
	{
		try
		{
			connection.remove(this);
			group.leave();
			connection.disconnect();
		}
		catch (SpreadException e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void subscribeOptimisticDelivery(
			OptimisticDeliverySubscriber optSubscriber)
	{
		throw new OptimisticDeliveryUnsupportedException();
	}

	public void sendTotalOrdered(byte[] payload)
	{
		SpreadMessage message = new SpreadMessage();
		message.setData(payload);
		message.addGroup(group);
		message.setAgreed();
		try
		{
			connection.multicast(message);
		}
		catch (SpreadException e)
		{
			System.err.println("Couldn't send message.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void sendReliably(byte[] payload)
	{
		SpreadMessage message = new SpreadMessage();
		message.setData(payload);
		message.addGroup(group);
		message.setReliable();
		try
		{
			connection.multicast(message);
		}
		catch (SpreadException e)
		{
			System.err.println("Couldn't send message.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void regularMessageReceived(SpreadMessage message)
	{
		byte[] payload = message.getData();
		Object obj = null;

		try
		{
			obj = ObjectSerializer.byteArray2Object(payload);
		}
		catch (GCPayloadException e)
		{
			return;
		}

		notifyDelivery(obj, new SpreadAddress(message.getSender()),
				payload.length);
	}

	public void membershipMessageReceived(SpreadMessage message)
	{
		final SpreadGroup[] members;
		if (message.isMembership()
				&& message.getMembershipInfo().isRegularMembership()
				&& (members = message.getMembershipInfo().getMembers()).length == Integer
						.getInteger("tribu.replicas").intValue()) {
			this.members = members;
			membersArrived();
		}
	}

	@Override
	public void sendTotalOrdered(byte[] payload, Group group)
	{
		System.err.println("Feature not implemented.");
		System.exit(-1);
	}

	@Override
	public void sendTo(byte[] payload, Address addr)
	{
		SpreadMessage message = new SpreadMessage();
		message.setData(payload);
		message.addGroup((SpreadGroup) addr.getSpecificAddress());
		message.setReliable();
		try
		{
			connection.multicast(message);
		}
		catch (SpreadException e)
		{
			System.err.println("Couldn't send message.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@Override
	public void sendToGroup(byte[] payload, Group group)
	{
		SpreadMessage message = new SpreadMessage();
		message.setData(payload);
		for (Address addr : group.getAll()) {
			message.addGroup((SpreadGroup) addr.getSpecificAddress());
		}
		message.setReliable();
		try
		{
			connection.multicast(message);
		}
		catch (SpreadException e)
		{
			System.err.println("Couldn't send message.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@Override
	public List<Address> getMembers()
	{
		final List<Address> addrs = new java.util.LinkedList<Address>();
		for (SpreadGroup addr : members) {
			addrs.add(new SpreadAddress(addr));
		}
		return addrs;
	}
}
