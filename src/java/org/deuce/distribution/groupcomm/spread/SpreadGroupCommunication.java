package org.deuce.distribution.groupcomm.spread;

import java.net.InetAddress;
import java.util.List;

import org.deuce.distribution.Defaults;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.GroupCommunication;
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

	@Override
	public void init()
	{
		connection = new SpreadConnection();
		group = new SpreadGroup();
		try
		{
			String daemon = System.getProperty(Defaults._COMM_SPREAD_DAEMON,
					Defaults.COMM_SPREAD_DAEMON);
			String privateName = "replica" + Integer.getInteger(Defaults._SITE);
			connection.connect(InetAddress.getByName(daemon), 0, privateName,
					false, true);
			connection.add(this);
			group.join(connection, System.getProperty(Defaults._COMM_GROUP,
					Defaults.COMM_GROUP));

			myAddress = new SpreadAddress(connection.getPrivateGroup()
					.toString());
		}
		catch (Exception e)
		{
			System.err.println("Couldn't initialise Spread client.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@Override
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

	@Override
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

	@Override
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

		notifyDelivery(obj, new SpreadAddress(message.getSender().toString()),
				payload.length);
	}

	@Override
	public void membershipMessageReceived(SpreadMessage message)
	{
		SpreadGroup[] members = null;
		if (message.isMembership()
				&& message.getMembershipInfo().isRegularMembership()
				&& (members = message.getMembershipInfo().getMembers()).length == Integer
						.getInteger(Defaults._REPLICAS).intValue())
		{
			this.members = members;
			membersArrived();
		}
		System.err.println("N_VIEW: " + members);
	}

	@Override
	public void sendTotalOrdered(byte[] payload, Group group)
	{
		System.err.println("Feature not implemented.");
		System.exit(-1);
	}

	@Override
	public void sendReliably(byte[] payload, Address addr)
	{
		SpreadMessage message = new SpreadMessage();
		message.setData(payload);
		message.addGroup((String) addr.getSpecificAddress());
		message.setReliable();
		message.setFifo();

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
	public void sendReliably(byte[] payload, Group group)
	{
		SpreadMessage message = new SpreadMessage();
		message.setData(payload);
		for (Address addr : group.getMembers())
		{
			message.addGroup((String) addr.getSpecificAddress());
		}

		message.setReliable();
		message.setFifo();

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
		for (SpreadGroup addr : members)
		{
			addrs.add(new SpreadAddress(addr.toString()));
		}
		return addrs;
	}
}
