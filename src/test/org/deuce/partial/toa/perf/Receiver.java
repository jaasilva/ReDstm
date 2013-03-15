package org.deuce.partial.toa.perf;

import java.util.Random;

import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.GroupCommunication;
import org.deuce.distribution.groupcomm.jgroups.JGroupsGroupCommunication;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;

public class Receiver implements Runnable, DeliverySubscriber
{
	private static int n_threads = 5;
	private static int n_msgs = 10000;
	private Address addr;
	private int id;
	private static Random rand = new Random();
	private long start;
	private int msgs;
	private int received;
	private GroupCommunication groupComm;

	public static void main(String[] args)
	{
		Thread[] threads = new Thread[n_threads];
		for (int i = 0; i < n_threads; i++)
		{
			threads[i] = new Thread(new Receiver(i + 1));
			threads[i].start();
		}
	}

	public Receiver(int x)
	{
		id = x;
		msgs = (Integer.getInteger("tribu.replicas") / 5) * n_msgs;
		received = 0;
	}

	@Override
	public void run()
	{
		groupComm = new JGroupsGroupCommunication();
		addr = groupComm.getAddress();
		groupComm.subscribeDelivery(this);

		if (id == n_threads)
		{ // SENDER
			// System.out
			// .println("SENDER: " + id + " - " + groupComm.getAddress());
			try
			{
				Thread.sleep(5000);
				System.out.println(id + "SENDER READY!!!");
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			for (int i = 0; i < n_msgs; i++)
			{
				groupComm.sendTotalOrdered(ObjectSerializer
						.object2ByteArray(new Byte((byte) 0)));
				// try
				// {
				// Thread.sleep(rand.nextInt(5000));
				// }
				// catch (InterruptedException e)
				// {
				// e.printStackTrace();
				// }
			}
		}
		else
		{ // RECEIVER
			// System.out.println("RECEIVER: " + id + " - "
			// + groupComm.getAddress());
		}
	}

	@Override
	public void onDelivery(Object obj, Address src, int size)
	{
		if (received == 0)
		{
			start = System.nanoTime();
		}

		// System.out.println(id + ">>>>> RECEIVED MESSAGE: " + addr
		// + " received from " + src + " = '" + obj + "'");
		received++;

		if (received == msgs)
		{
			System.out.println(id + ": " + (System.nanoTime() - start)
					/ 1000000 + "ms");
			groupComm.close();
		}
	}
}