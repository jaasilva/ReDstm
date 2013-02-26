package org.deuce.distribution.groupcomm.jgroups;

import java.io.InputStream;
import java.io.OutputStream;

import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.GroupCommunication;
import org.deuce.distribution.groupcomm.OptimisticDeliveryUnsupportedException;
import org.deuce.distribution.groupcomm.subscriber.OptimisticDeliverySubscriber;
import org.deuce.distribution.serialization.GCPayloadException;
import org.deuce.transform.ExcludeTM;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.Receiver;
import org.jgroups.View;


@ExcludeTM
public class JGroupsGroupCommunication extends GroupCommunication implements
		Receiver {

	private JChannel channelTOB;
//	private JChannel channelRB;
	
	public JGroupsGroupCommunication() {
		super();
	}

	public void init() {
		try {
			channelTOB = new JChannel("etc/jgroups.xml");
			channelTOB.setReceiver(this);
			channelTOB.connect(System.getProperty("tribu.groupcommunication.group", "tvale"));
			
//			channelRB = new JChannel("etc/jgroups.xml");
//			channelRB.setReceiver(this);
//			channelRB.connect(System.getProperty("tribu.groupcommunication.group", "tvale")+"-rb");
			
			myAddress = new JGroupsAddress(channelTOB.getAddress());
		} catch (Exception e) {
			System.err.println("Couldn't initialise JGroups.");
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	public void close() {
		channelTOB.close();
	}

	@Override
	public void subscribeOptimisticDelivery(
			OptimisticDeliverySubscriber optSubscriber) {
		throw new OptimisticDeliveryUnsupportedException();
	}
	
	public void sendTo(byte[] payload, Address addr)
	{
		final Message msg = new Message();
//		msg.setDest(addr);
		// TODO .........................................................................
		msg.setBuffer(payload);
		msg.setFlag(Message.NO_TOTAL_ORDER);
		try {
			channelTOB.send(msg);
		} catch (Exception e) {
			System.err.println("Couldn't send message.");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public void sendTotalOrdered(final byte[] payload) {
//		new Thread(new Runnable() {
//			public void run() {
				try {
					channelTOB.send(null, payload);
				} catch (Exception e) {
					System.err.println("Couldn't send message.");
					e.printStackTrace();
					System.exit(-1);
				}
//			}
//		}).start();
	}

	public void sendReliably(byte[] payload) {
		final Message msg = new Message();
		msg.setDest(null);
		msg.setBuffer(payload);
		msg.setFlag(Message.NO_TOTAL_ORDER);
//		new Thread(new Runnable() {
//			public void run() {
				try {
					channelTOB.send(msg);
				} catch (Exception e) {
					System.err.println("Couldn't send message.");
					e.printStackTrace();
					System.exit(-1);
				}
//			}
//		}).start();
	}

	public void receive(Message msg) {
		byte[] payload = msg.getRawBuffer();
		Object obj = null;

		try {
			obj = ObjectSerializer.byteArray2Object(payload);
		} catch (GCPayloadException e) {
			return;
		}

		notifyDelivery(obj, new JGroupsAddress(msg.getSrc()),
				payload.length);
	}

	public void getState(OutputStream output) throws Exception {
		// TODO Auto-generated method stub

	}

	public void setState(InputStream input) throws Exception {
		// TODO Auto-generated method stub

	}

	public void viewAccepted(View new_view) {
		if (new_view.getMembers().size() == Integer
				.getInteger("tribu.replicas"))
			membersArrived();
	}

	public void suspect(org.jgroups.Address suspected_mbr) {
		// TODO Auto-generated method stub

	}

	public void block() {
		// TODO Auto-generated method stub

	}

	public void unblock() {
		// TODO Auto-generated method stub

	}
}
