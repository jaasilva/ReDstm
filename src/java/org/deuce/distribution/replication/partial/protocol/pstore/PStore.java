package org.deuce.distribution.replication.partial.protocol.pstore;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.partial.PartialReplicationProtocol;
import org.deuce.transaction.DistributedContext;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class PStore extends PartialReplicationProtocol implements
		DeliverySubscriber
{

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.DistributedProtocol#init()
	 */
	@Override
	public void init()
	{
		// TODO init PStore
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.DistributedProtocol#onTxContextCreation(org.deuce
	 * .transaction.DistributedContext)
	 */
	@Override
	public void onTxContextCreation(DistributedContext ctx)
	{
		// TODO onTxContextCreation PStore
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.DistributedProtocol#onTxBegin(org.deuce.transaction
	 * .DistributedContext)
	 */
	@Override
	public void onTxBegin(DistributedContext ctx)
	{
		// TODO onTxBegin PStore
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.DistributedProtocol#onTxCommit(org.deuce.transaction
	 * .DistributedContext)
	 */
	@Override
	public void onTxCommit(DistributedContext ctx)
	{
		// TODO onTxCommit PStore
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.DistributedProtocol#onTxFinished(org.deuce.transaction
	 * .DistributedContext, boolean)
	 */
	@Override
	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
		// TODO onTxFinished PStore
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.DistributedProtocol#onTxRead(org.deuce.transaction
	 * .DistributedContext, org.deuce.distribution.ObjectMetadata)
	 */
	@Override
	public Object onTxRead(DistributedContext ctx, ObjectMetadata metadata)
	{
		// TODO onTxRead PStore
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.DistributedProtocol#onTxWrite(org.deuce.transaction
	 * .DistributedContext, org.deuce.distribution.ObjectMetadata,
	 * org.deuce.distribution.UniqueObject)
	 */
	@Override
	public void onTxWrite(DistributedContext ctx, ObjectMetadata metadata,
			UniqueObject obj)
	{
		// TODO onTxWrite PStore
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber#onDelivery
	 * (java.lang.Object, org.deuce.distribution.groupcomm.Address, int)
	 */
	@Override
	public void onDelivery(Object obj, Address src, int size)
	{
		// TODO onDelivery PStore
	}

}
