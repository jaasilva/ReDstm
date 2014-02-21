package test;

import java.util.Random;

import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;

public class UnicastTest implements DeliverySubscriber
{
	public static void main(String[] args) throws InterruptedException
	{
		TribuDSTM.init();
		Group group = new PartialReplicationGroup(TribuDSTM.ALL);
		TribuDSTM.subscribeDeliveries(new UnicastTest());
		Random r = new Random();

		for (int i = 0; i < 10; i++)
		{
			byte[] payload = ObjectSerializer.object2ByteArray(new Integer(i));
			Thread.sleep(r.nextInt(1000));
			TribuDSTM.sendReliably(payload, group);
		}
	}

	@Override
	public void onDelivery(Object obj, Address src, int size)
	{
		System.out.println(src + " -> " + (Integer) obj);
	}
}
