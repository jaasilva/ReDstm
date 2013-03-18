package org.deuce.distribution.groupcomm.jgroups;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.GroupCommunication;
import org.deuce.distribution.groupcomm.OptimisticDeliveryUnsupportedException;
import org.deuce.distribution.groupcomm.subscriber.OptimisticDeliverySubscriber;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.serialization.GCPayloadException;
import org.deuce.transform.ExcludeTM;
import org.jgroups.AnycastAddress;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;

@ExcludeTM
public class JGroupsGroupCommunication extends GroupCommunication implements
		Receiver
{
	private static final Logger LOGGER = Logger
			.getLogger(JGroupsGroupCommunication.class);
	private JChannel channel;

	public JGroupsGroupCommunication()
	{
		super();
	}

	public void init()
	{
		try
		{ // TODO check config in TOA "toa.xml"
			channel = new JChannel("etc/jgroups.xml");
			channel.setReceiver(this);
			channel.connect(System.getProperty(
					"tribu.groupcommunication.group", "tvale"));

			myAddress = new JGroupsAddress(channel.getAddress());
		}
		catch (Exception e)
		{
			System.err.println("Couldn't initialise JGroups.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public List<Address> getMembers()
	{
		List<org.jgroups.Address> members = channel.getView().getMembers();
		List<Address> addrs = new ArrayList<Address>(members.size());

		for (org.jgroups.Address a : members)
		{
			addrs.add(new JGroupsAddress(a));
		}

		return addrs;
	}

	public void close()
	{
		channel.close();
	}

	@Override
	public void subscribeOptimisticDelivery(
			OptimisticDeliverySubscriber optSubscriber)
	{
		throw new OptimisticDeliveryUnsupportedException();
	}

	public void sendTotalOrdered(final byte[] payload)
	{
		try
		{
			channel.send(null, payload);
		}
		catch (Exception e)
		{
			System.err.println("Couldn't send message.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void sendTotalOrdered(byte[] payload, Group group)
	{ // TODO ver maneira melhor
		AnycastAddress addr = new AnycastAddress();
		for (Address a : group.getAddresses())
		{
			addr.add((org.jgroups.Address) a.getAddress());
		}

		try
		{
			channel.send(addr, payload);
		}
		catch (Exception e)
		{
			System.err.println("Couldn't send message.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void sendReliably(byte[] payload)
	{
		final Message msg = new Message();
		msg.setDest(null);
		msg.setBuffer(payload);
		msg.setFlag(Message.NO_TOTAL_ORDER);

		try
		{
			channel.send(msg);
		}
		catch (Exception e)
		{
			System.err.println("Couldn't send message.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void receive(Message msg)
	{
		byte[] payload = msg.getRawBuffer();
		Object obj = null;

		try
		{
			obj = ObjectSerializer.byteArray2Object(payload);
		}
		catch (GCPayloadException e)
		{
			return;
		}

		notifyDelivery(obj, new JGroupsAddress(msg.getSrc()), payload.length);
	}

	public void getState(OutputStream output) throws Exception
	{ // nothing to do
	}

	public void setState(InputStream input) throws Exception
	{ // nothing to do
	}

	public void viewAccepted(View new_view)
	{
		LOGGER.debug(String.format("New group view: %s", new_view.toString()));
		if (new_view.getMembers().size() == Integer
				.getInteger("tribu.replicas"))
			membersArrived();
	}

	public void suspect(org.jgroups.Address suspected_mbr)
	{ // nothing to do
	}

	public void block()
	{ // nothing to do
	}

	public void unblock()
	{ // nothing to do
	}
}
