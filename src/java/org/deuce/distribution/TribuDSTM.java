package org.deuce.distribution;

import java.util.List;

import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.GroupCommunication;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.groupcomm.subscriber.OptimisticDeliverySubscriber;
import org.deuce.distribution.location.SimpleLocator;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partitioner.data.DataPartitioner;
import org.deuce.distribution.replication.partitioner.group.GroupPartitioner;
import org.deuce.objectweb.asm.Type;
import org.deuce.transaction.DistributedContext;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public final class TribuDSTM
{
	public static final String DESC = Type.getDescriptor(TribuDSTM.class);
	public static final String NAME = Type.getInternalName(TribuDSTM.class);

	private static final Locator locator = new SimpleLocator();
	private static DistributedProtocol distProtocol;
	private static GroupCommunication groupComm;
	private static DataPartitioner dataPartitioner;
	private static GroupPartitioner groupPartitioner;

	private static Class<? extends Group> groupClass;
	private static Class<? extends DistributedContext> ctxClass;

	static
	{
		initReplicationProtocol();
		initTransactionContext();
		initPartitioners();
	}

	/*
	 * Defer the initialization of group communication until the actual
	 * application is executing. Apparently, explicit class loading during the
	 * execution of an agent is only supported in JDK 7.
	 */
	public static final String INIT_METHOD_NAME = "init";
	public static final String INIT_METHOD_DESC = "()"
			+ Type.VOID_TYPE.getDescriptor();

	public static void init()
	{
		initGroupCommunication();
		groupPartitioner.partitionGroups(groupComm.getMembers(),
				Integer.getInteger("tribu.groups", 1));
		dataPartitioner.init();
		distProtocol.init();
	}

	public static final String CLOSE_METHOD_NAME = "close";
	public static final String CLOSE_METHOD_DESC = "()"
			+ Type.VOID_TYPE.getDescriptor();

	public static void close()
	{
		groupComm.close();
	}

	private static void initGroupCommunication()
	{
		String className = System
				.getProperty("tribu.groupcommunication.class",
						"org.deuce.distribution.groupcomm.jgroups.JGroupsGroupCommunication");

		try
		{
			@SuppressWarnings("unchecked")
			Class<? extends GroupCommunication> groupCommClass = (Class<? extends GroupCommunication>) Class
					.forName(className);
			groupComm = groupCommClass.newInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

	@SuppressWarnings("unchecked")
	private static void initTransactionContext()
	{
		String className = System.getProperty(
				"org.deuce.transaction.contextClass",
				"org.deuce.transaction.tl2.Context");

		try
		{
			ctxClass = (Class<? extends DistributedContext>) Class
					.forName(className);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static void initReplicationProtocol()
	{
		String className = System
				.getProperty("tribu.distributed.protocolClass",
						"org.deuce.distribution.replication.full.protocol.nonvoting.NonVoting");

		try
		{
			@SuppressWarnings("unchecked")
			Class<? extends DistributedProtocol> distProtocolClass = (Class<? extends DistributedProtocol>) Class
					.forName(className);
			distProtocol = distProtocolClass.newInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static final List<Address> getMembers()
	{ // XXX remove
		return groupComm.getMembers();
	}

	@SuppressWarnings("unchecked")
	private static void initPartitioners()
	{
		String groupPartClass = System
				.getProperty("tribu.distributed.GroupPartitionerClass",
						"org.deuce.distribution.replication.partitioner.group.RandomGroupPartitioner");
		String dataPartClass = System
				.getProperty("tribu.distributed.DataPartitionerClass",
						"org.deuce.distribution.replication.partitioner.data.SimpleDataPartitioner");
		String gClass = System
				.getProperty("tribu.distributed.GroupClass",
						"org.deuce.distribution.replication.group.PartialReplicationGroup");

		try
		{
			Class<? extends GroupPartitioner> groupPart = (Class<? extends GroupPartitioner>) Class
					.forName(groupPartClass);
			groupPartitioner = groupPart.newInstance();

			Class<? extends DataPartitioner> dataPart = (Class<? extends DataPartitioner>) Class
					.forName(dataPartClass);
			dataPartitioner = dataPart.newInstance();

			groupClass = (Class<? extends Group>) Class.forName(gClass);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}

	public static final Class<? extends DistributedContext> getContextClass()
	{
		return ctxClass;
	}

	public static final Class<? extends Group> getGroupClass()
	{
		return groupClass;
	}

	public static final Locator getLocator()
	{
		return locator;
	}

	public static final UniqueObject getObject(ObjectMetadata metadata)
	{
		return locator.get(metadata);
	}

	public static final void putObject(ObjectMetadata metadata, UniqueObject obj)
	{
		locator.put(metadata, obj);
	}

	public static final void onContextCreation(DistributedContext ctx)
	{
		distProtocol.onTxContextCreation(ctx);
	}

	public static final void onTxBegin(DistributedContext ctx)
	{
		distProtocol.onTxBegin(ctx);
	}

	public static final void onTxFinished(DistributedContext ctx,
			boolean committed)
	{
		distProtocol.onTxFinished(ctx, committed);
	}

	public static final void onTxCommit(DistributedContext ctx)
	{
		distProtocol.onTxCommit(ctx);
	}

	public static final Object onTxRead(DistributedContext ctx,
			ObjectMetadata metadata)
	{
		return distProtocol.onTxRead(ctx, metadata);
	}

	public static final void onTxWrite(DistributedContext ctx,
			ObjectMetadata metadata, UniqueObject obj)
	{
		distProtocol.onTxWrite(ctx, metadata, obj);
	}

	public static final String GETSERIALIZER_METHOD_NAME = "getObjectSerializer";
	public static final String GETSERIALIZER_METHOD_DESC = "()"
			+ ObjectSerializer.DESC;

	public static final ObjectSerializer getObjectSerializer()
	{
		return distProtocol.getObjectSerializer();
	}

	public static final void sendTotalOrdered(byte[] payload)
	{
		groupComm.sendTotalOrdered(payload);
	}

	public static final void sendTotalOrdered(byte[] payload, Group group)
	{
		groupComm.sendTotalOrdered(payload, group);
	}

	public static final void sendReliably(byte[] payload)
	{
		groupComm.sendReliably(payload);
	}

	public static final boolean isLocalAddress(Address addr)
	{
		return groupComm.isLocal(addr);
	}

	public static final void subscribeDeliveries(DeliverySubscriber subscriber)
	{
		groupComm.subscribeDelivery(subscriber);
	}

	public static final void subscribeOptimisticDeliveries(
			OptimisticDeliverySubscriber subscriber)
	{
		groupComm.subscribeOptimisticDelivery(subscriber);
	}

	public static final Address getAddress()
	{
		return groupComm.getAddress();
	}

	public static final Group getMyGroup()
	{
		return groupPartitioner.getMyGroup();
	}

	public static final List<Group> getAllGroups()
	{
		return groupPartitioner.getGroups();
	}

	public static final Group publishObjectTo(UniqueObject obj)
	{
		return dataPartitioner.publishTo(obj);
	}
}
