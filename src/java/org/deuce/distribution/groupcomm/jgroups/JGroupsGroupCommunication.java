package org.deuce.distribution.groupcomm.jgroups;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.deuce.distribution.Defaults;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.GroupCommunication;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.PartialReplicationProtocol;
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
	private JChannel channel;

	public JGroupsGroupCommunication()
	{
		super();
	}

	@Override
	public void init()
	{
		try
		{
			channel = new JChannel(Defaults.JGROUPS_CONFIG_FILE);
			channel.setReceiver(this);
			channel.connect(System.getProperty(Defaults._COMM_GROUP,
					Defaults.COMM_GROUP));

			myAddress = new JGroupsAddress(channel.getAddress());
		}
		catch (Exception e)
		{
			System.err.println("Couldn't initialise JGroups.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@Override
	public void close()
	{
		channel.close();
	}

	@Override
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

	@Override
	public void sendReliably(byte[] payload)
	{
		final Message msg = new Message();
		msg.setDest(null);
		msg.setBuffer(payload);
		msg.setFlag(Message.Flag.NO_TOTAL_ORDER);

		if (PartialReplicationProtocol.serializationReadCtx.get())
		{/*
		 * *OPTIMIZATION* If in a read context, the message can be passed
		 * out-of-band safely and avoid the protocol stack overhead.
		 */
			msg.setFlag(Message.Flag.OOB);
		}

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

	@Override
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

	@Override
	public void getState(OutputStream output) throws Exception
	{
	}

	@Override
	public void setState(InputStream input) throws Exception
	{
	}

	@Override
	public void viewAccepted(View new_view)
	{
		System.err.println("N_VIEW: " + new_view);
		if (new_view.getMembers().size() == Integer
				.getInteger(Defaults._REPLICAS))
			membersArrived();
	}

	@Override
	public void suspect(org.jgroups.Address suspected_mbr)
	{
	}

	@Override
	public void block()
	{
	}

	@Override
	public void unblock()
	{
	}

	@Override
	public void sendTotalOrdered(byte[] payload, Group group)
	{
		AnycastAddress addr = new AnycastAddress();
		for (Address a : group.getAll())
		{ // assumes group has no duplicate addresses
			addr.add((org.jgroups.Address) a.getSpecificAddress());
		}

		final Message msg = new Message();
		msg.setDest(addr);
		msg.setBuffer(payload);

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

	@Override
	public void sendReliably(byte[] payload, Address addr)
	{
		try
		{
			final Message msg = new Message(
					(org.jgroups.Address) addr.getSpecificAddress(), payload);

			if (PartialReplicationProtocol.serializationReadCtx.get())
			{/*
			 * *OPTIMIZATION* If in a read context, the message can be passed
			 * out-of-band safely and avoid the protocol stack overhead.
			 */
				msg.setFlag(Message.Flag.OOB);
			}

			channel.send(msg);
		}
		catch (Exception e)
		{
			System.err.println("Couldn't send message.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@Override
	public void sendReliably(byte[] payload, Group group)
	{
		if (group.isAll())
		{ // if group isAll, we can multicast instead of doing the for cycle
			sendReliably(payload);
		}
		else
		{
			for (Address a : group.getAll())
			{ // assumes group has no duplicate addresses
				try
				{
					channel.send((org.jgroups.Address) a.getSpecificAddress(),
							payload);
				}
				catch (Exception e)
				{
					System.err.println("Couldn't send message.");
					e.printStackTrace();
					System.exit(-1);
				}
			}
		}
	}

	@Override
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
}
