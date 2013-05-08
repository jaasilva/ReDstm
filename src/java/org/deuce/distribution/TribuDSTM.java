package org.deuce.distribution;

import java.util.Collection;

import org.apache.log4j.Logger;
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
public class TribuDSTM
{
	private static final Logger LOGGER = Logger.getLogger(TribuDSTM.class);
	public static final String DESC = Type.getDescriptor(TribuDSTM.class);
	public static final String NAME = Type.getInternalName(TribuDSTM.class);

	private static final Locator locator = new SimpleLocator();
	private static DistributedProtocol distProtocol;
	private static GroupCommunication groupComm;
	private static DataPartitioner dataPart;
	private static GroupPartitioner groupPart;
	private static boolean PARTIAL;

	public static Group ALL;
	public static String partialDefault = "false";

	private static Class<? extends DistributedContext> ctxClass;

	static
	{
		LOGGER.info("TribuDSTM initializing...");
		initReplicationProtocol();
		initTransactionContext();
	}

	/*
	 * Defer the initialisation of group communication until the actual
	 * application is executing. Apparently, explicit class loading during the
	 * execution of an agent is only supported in JDK 7.
	 */
	public static final String INIT_METHOD_NAME = "init";
	public static final String INIT_METHOD_DESC = "()"
			+ Type.VOID_TYPE.getDescriptor();

	public static void init()
	{
		initGroupCommunication();
		distProtocol.init();

		LOGGER.info("TribuDSTM initialized");
	}

	public static final String CLOSE_METHOD_NAME = "close";
	public static final String CLOSE_METHOD_DESC = "()"
			+ Type.VOID_TYPE.getDescriptor();

	public static void close()
	{
		LOGGER.info("TribuDSTM closing");

		groupComm.close();

		LOGGER.info("TribuDSTM closing");
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

		LOGGER.info("Initializing group comunication: " + className);
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

		LOGGER.info("Initializing transaction context: " + className);
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

		LOGGER.info("Initializing replication protocol: " + className);
	}

	public static final Class<? extends DistributedContext> getContextClass()
	{
		return ctxClass;
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

	public static final Collection<Address> getAllMembers()
	{
		return groupComm.getMembers();
	}

	public static final Address getLocalAddress()
	{
		return groupComm.getLocalAddress();
	}

	public static final Group publishObjectTo(UniqueObject obj)
	{
		return dataPart.publishTo(obj);
	}

	public static final Group getLocalGroup()
	{
		return groupPart.getLocalGroup();
	}

	public static final boolean isLocalGroup(Group group)
	{
		return group.contains(getLocalAddress());
	}

	public static final boolean groupIsAll(Group group)
	{
		return group.equals(ALL);
	}

	public static final void sendTotalOrdered(byte[] payload, Group group)
	{
		groupComm.sendTotalOrdered(payload, group);
	}

	public static final void sendTo(byte[] payload, Address addr)
	{
		groupComm.sendTo(payload, addr);
	}

	public static final void sendToGroup(byte[] payload, Group group)
	{
		groupComm.sendToGroup(payload, group);
	}
}
