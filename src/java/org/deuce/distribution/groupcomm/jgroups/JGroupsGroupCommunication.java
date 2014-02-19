package org.deuce.distribution.groupcomm.jgroups;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.deuce.Defaults;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
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
	private int numNodes;

	public JGroupsGroupCommunication()
	{
		super();
	}

	public void init()
	{
		try
		{
			channel = new JChannel("etc/jgroups.xml");

			if (TribuDSTM.PARTIAL)
			{ // XXX Partial Replication Mode (increase receiver threads)
				channel.getProtocolStack()
						.findProtocol("UDP")
						.setValue("thread_pool_max_threads",
								Defaults.JGROUPS_PARTIAL_MAX_RECEIVER_THREADS);
			}

			channel.setReceiver(this);
			channel.connect(System.getProperty(Defaults._COMM_GROUP,
					Defaults.COMM_GROUP));

			myAddress = new JGroupsAddress(channel.getAddress());
			numNodes = Integer.getInteger(Defaults._REPLICAS);
		}
		catch (Exception e)
		{
			System.err.println("Couldn't initialise JGroups.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void close()
	{
		channel.close();
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

	public void sendReliably(byte[] payload)
	{
		final Message msg = new Message();
		msg.setDest(null);
		msg.setBuffer(payload);
		msg.setFlag(Message.Flag.NO_TOTAL_ORDER);

		if (PartialReplicationProtocol.serializationReadCtx.get())
		{
			msg.setFlag(Message.Flag.OOB); // XXX trying to optimize
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
	{
	}

	public void setState(InputStream input) throws Exception
	{
	}

	public void viewAccepted(View new_view)
	{
		// System.err.println("N_VIEW: " + new_view);
		if (new_view.getMembers().size() == Integer
				.getInteger(Defaults._REPLICAS))
			membersArrived();
	}

	public void suspect(org.jgroups.Address suspected_mbr)
	{
	}

	public void block()
	{
	}

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
	public void sendTo(byte[] payload, Address addr)
	{
		try
		{
			final Message msg = new Message(
					(org.jgroups.Address) addr.getSpecificAddress(), payload);
			if (PartialReplicationProtocol.serializationReadCtx.get())
			{
				msg.setFlag(Message.Flag.OOB); // XXX trying to optimize
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
	public void sendToGroup(byte[] payload, Group group)
	{
		if (group.size() == numNodes)
		{
			sendReliably(payload); // XXX trying to optimize
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
