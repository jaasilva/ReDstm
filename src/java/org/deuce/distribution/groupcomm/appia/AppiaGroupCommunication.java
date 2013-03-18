package org.deuce.distribution.groupcomm.appia;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.GroupCommunication;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.serialization.GCPayloadException;
import org.deuce.transform.ExcludeTM;

import net.sf.appia.jgcs.AppiaGroup;
import net.sf.appia.jgcs.AppiaProtocolFactory;
import net.sf.appia.jgcs.AppiaService;
import net.sf.jgcs.DataSession;
import net.sf.jgcs.ExceptionListener;
import net.sf.jgcs.JGCSException;
import net.sf.jgcs.Message;
import net.sf.jgcs.MessageListener;
import net.sf.jgcs.NotJoinedException;
import net.sf.jgcs.Protocol;
import net.sf.jgcs.Service;
import net.sf.jgcs.ServiceListener;
import net.sf.jgcs.UnsupportedServiceException;
import net.sf.jgcs.membership.BlockListener;
import net.sf.jgcs.membership.BlockSession;
import net.sf.jgcs.membership.Membership;
import net.sf.jgcs.membership.MembershipListener;

@ExcludeTM
public class AppiaGroupCommunication extends GroupCommunication implements
		ExceptionListener, MessageListener, ServiceListener, BlockListener,
		MembershipListener
{
	private static final Logger LOGGER = Logger
			.getLogger(AppiaGroupCommunication.class);
	private Protocol protocol;
	private AppiaGroup config;
	private AppiaService sendTOService, sendURBService, recvService;
	private DataSession dataSession;
	private BlockSession controlSession;

	public AppiaGroupCommunication()
	{
		super();
	}

	public void init()
	{
		config = new AppiaGroup();
		config.setGroupName(System.getProperty(
				"tribu.groupcommunication.group", "tvale"));
		config.setConfigFileName("etc/appia-tob.xml");
		try
		{
			protocol = new AppiaProtocolFactory().createProtocol();
			sendTOService = new AppiaService("vsc+total+services");
			recvService = new AppiaService("uniform_total_order");
			dataSession = protocol.openDataSession(config);
			controlSession = (BlockSession) protocol.openControlSession(config);
			dataSession.setExceptionListener(this);
			dataSession.setMessageListener(this);
			dataSession.setServiceListener(this);
			controlSession.setBlockListener(this);
			controlSession.setMembershipListener(this);
			controlSession.join();
			myAddress = new AppiaAddress(controlSession.getLocalAddress());
		}
		catch (JGCSException e)
		{
			System.err.println("Couldn't initialise Appia.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public List<Address> getMembers()
	{
		List<Address> addrs = null;

		try
		{
			List<SocketAddress> list = controlSession.getMembership()
					.getMembershipList();
			addrs = new ArrayList<Address>(list.size());

			for (SocketAddress a : list)
			{
				addrs.add(new AppiaAddress(a));
			}
		}
		catch (NotJoinedException e)
		{
			e.printStackTrace();
		}

		return addrs;
	}

	public void close()
	{
		try
		{
			controlSession.leave();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		dataSession.close();
	}

	public void sendTotalOrdered(byte[] payload)
	{
		try
		{
			Message msg = dataSession.createMessage();
			msg.setPayload(payload);
			dataSession.multicast(msg, sendTOService, null);
		}
		catch (Exception e)
		{
			System.err.println("Couldn't send message.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void sendTotalOrdered(byte[] payload, Group group)
	{
		System.err.println("Feature not implemented.");
		System.exit(-1);
	}

	public void sendReliably(byte[] payload)
	{
		try
		{
			Message msg = dataSession.createMessage();
			msg.setPayload(payload);
			dataSession.multicast(msg, sendURBService, null);
		}
		catch (Exception e)
		{
			System.err.println("Couldn't send message.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void onMembershipChange()
	{
		Membership membership;
		try
		{
			membership = controlSession.getMembership();
			LOGGER.debug(String.format("New group view: %s",
					membership.toString()));
			if (membership.getMembershipList().size() == Integer.getInteger(
					"tribu.replicas").intValue())
				membersArrived();
		}
		catch (NotJoinedException e)
		{
			e.printStackTrace();
		}
	}

	public void onExcluded()
	{
		// nothing to do
	}

	public void onBlock()
	{
		try
		{
			controlSession.blockOk();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void onServiceEnsured(Object context, Service service)
	{
		try
		{
			if (service.compare(recvService) >= 0)
			{
				Tuple tuple = (Tuple) context;
				notifyDelivery(tuple.obj, tuple.addr, tuple.payloadSize);
			}
		}
		catch (UnsupportedServiceException e)
		{
			System.err.println("Received an unsupported service.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public Object onMessage(Message msg)
	{
		byte[] payload = msg.getPayload();
		Object obj = null;

		try
		{
			obj = ObjectSerializer.byteArray2Object(payload);
		}
		catch (GCPayloadException e)
		{
			return null;
		}

		Address src = new AppiaAddress(msg.getSenderAddress());

		obj = notifyOptimisticDelivery(obj, src, payload.length);

		return new Tuple(obj, src, payload.length);
	}

	public void onException(JGCSException exception)
	{
		// nothing to do
	}

	@ExcludeTM
	private class Tuple
	{
		Object obj;
		Address addr;
		int payloadSize;

		public Tuple(Object obj, Address addr, int payloadSize)
		{
			super();
			this.obj = obj;
			this.addr = addr;
			this.payloadSize = payloadSize;
		}
	}
}
