package org.deuce.partial.toa.perf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Random;

import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.GroupCommunication;
import org.deuce.distribution.groupcomm.jgroups.JGroupsGroupCommunication;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;

public class Receiver implements Runnable, DeliverySubscriber
{
	private static int n_threads;
	private static int n_msgs;
	private Address addr;
	private int id;
	private static int msg_size;
	private static Random rand = new Random();
	private long start;
	private int msgs;
	private int received;
	private GroupCommunication groupComm;
	private static String f;
	private static Object lock = 0;

	public static void main(String[] args)
	{
		f = args[0];
		msg_size = Integer.parseInt(args[1]);
		n_threads = Integer.parseInt(args[2]);
		n_msgs = Integer.parseInt(args[3]);
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
		msgs = (Integer.getInteger("tribu.replicas") / n_threads) * n_msgs;
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
				Thread.sleep(2000);
				// System.out.println("SENDER READY!!!");
			}
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}

			long st = System.nanoTime();
			for (int i = 0; i < n_msgs; i++)
			{

				groupComm.sendTotalOrdered(ObjectSerializer
						.object2ByteArray(new byte[msg_size - 27]));

				// try
				// {
				// Thread.sleep(rand.nextInt(5000));
				// }
				// catch (InterruptedException e)
				// {
				// e.printStackTrace();
				// }
			}
			long sto = System.nanoTime() - st;
			try
			{
				synchronized (lock)
				{
					PrintWriter pw = new PrintWriter(new FileOutputStream(
							new File("logs/log-" + f), true));
					pw.write(id + ":::: " + sto / 1000000 + "\n");
					pw.flush();
					pw.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
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
			// System.out.println(size);
			start = System.nanoTime();
		}

		// System.out.println(id + ">>>>> RECEIVED MESSAGE: " + addr
		// + " received from " + src + " = '" + obj + "'");
		received++;

		if (received == msgs)
		{
			long stop = System.nanoTime() - start;
			// System.out.println(id + ": " + stop / 1000000 + "ms");

			try
			{
				synchronized (lock)
				{
					PrintWriter pw = new PrintWriter(new FileOutputStream(
							new File("logs/log-" + f), true));
					pw.write(id + ": " + stop / 1000000 + "\n");
					pw.flush();
					pw.close();
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
			groupComm.close();
		}
	}
}
