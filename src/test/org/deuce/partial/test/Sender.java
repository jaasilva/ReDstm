package org.deuce.partial.test;

import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.group.PartialReplicationGroup;

public class Sender
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		TribuDSTM.init();

		Group a = new PartialReplicationGroup(TribuDSTM.getMembers());

		TribuDSTM.sendTotalOrdered(
				ObjectSerializer.object2ByteArray("Hello world"), a);
		
		Group b = TribuDSTM.getAllGroups().get(0);
		System.out.println(b);
		
		TribuDSTM.sendTotalOrdered(
				ObjectSerializer.object2ByteArray("Hello world22222222"), b);
	}

}
