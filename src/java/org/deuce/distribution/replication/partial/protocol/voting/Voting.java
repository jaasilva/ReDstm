package org.deuce.distribution.replication.partial.protocol.voting;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.full.FullReplicationProtocol;
import org.deuce.transaction.DistributedContext;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 */
public class Voting extends FullReplicationProtocol implements
		DeliverySubscriber
{
	@Override
	public void init()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onTxContextCreation(DistributedContext ctx)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onTxBegin(DistributedContext ctx)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onTxCommit(DistributedContext ctx)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object onTxRead(DistributedContext ctx, TxField field)
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onDelivery(Object obj, Address src, int size)
	{
		// TODO Auto-generated method stub

	}
}
