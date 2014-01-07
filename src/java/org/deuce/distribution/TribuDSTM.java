package org.deuce.distribution;

import java.util.Collection;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.deuce.Defaults;
import org.deuce.distribution.cache.Cache;
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
import org.deuce.transaction.score.field.Version;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * This class uses the facade design pattern. It functions as the glue between
 * all the other components in the system. It initializes and terminates the
 * system.
 * 
 * @author tvale, jaasilva
 */
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
	private static Class<? extends DistributedContext> ctxClass;
	private static Cache cache;
	public static boolean PARTIAL; // check runtime mode
	public static boolean CACHE;

	public static final Collection<Address> ALL = new HashSet<Address>();

	/*
	 * ################################################################
	 * ########### INITIALIZATION #####################################
	 * ################################################################
	 */

	static
	{
		LOGGER.warn("####################################################");
		LOGGER.warn("> TribuDSTM initializing...");

		checkRuntimeMode();
		initReplicationProtocol();
		initTransactionContext();

		if (PARTIAL)
		{
			initPartitioners();
			initCache();
			int groups = Integer.getInteger(Defaults._GROUPS, Defaults.GROUPS);
			groupPart.init(groups);
		}
	}

	/**
	 * Checks if the system will run in partial or total replication mode.
	 */
	private static void checkRuntimeMode()
	{
		PARTIAL = Boolean.parseBoolean(System.getProperty(
				Defaults._PARTIAL_MODE, Defaults.PARTIAL_MODE));

		LOGGER.warn("> Partial replication mode: " + PARTIAL);
	}

	/**
	 * Initializes the distributed protocol component.
	 */
	private static void initReplicationProtocol()
	{
		String className = System.getProperty(Defaults._PROTO_CLASS,
				Defaults.PROTO_CLASS);
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

		LOGGER.warn("> Replication protocol: " + className);
	}

	/**
	 * Initializes the transaction context used by the system.
	 */
	@SuppressWarnings("unchecked")
	private static void initTransactionContext()
	{
		String className = System.getProperty(Defaults._CTX_CLASS,
				Defaults.CTX_CLASS);
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

		LOGGER.warn("> Transaction context: " + className);
	}

	/**
	 * Initializes both the group and data partitioners.
	 */
	private static void initPartitioners()
	{
		String groupPartClass = System.getProperty(Defaults._GP_CLASS,
				Defaults.GP_CLASS);
		String dataPartClass = System.getProperty(Defaults._DP_CLASS,
				Defaults.DP_CLASS);
		try
		{
			@SuppressWarnings("unchecked")
			Class<? extends GroupPartitioner> groupP = (Class<? extends GroupPartitioner>) Class
					.forName(groupPartClass);
			groupPart = groupP.newInstance();
			@SuppressWarnings("unchecked")
			Class<? extends DataPartitioner> dataP = (Class<? extends DataPartitioner>) Class
					.forName(dataPartClass);
			dataPart = dataP.newInstance();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}

		LOGGER.warn("> Group partitioner: " + groupPartClass);
		LOGGER.warn("> Data partitioner: " + dataPartClass);
	}

	/**
	 * Initializes the remote objects' cache.
	 */
	private static void initCache()
	{
		cache = new Cache();
		CACHE = true;
	}

	public static final String INIT_METHOD_NAME = "init";
	public static final String INIT_METHOD_DESC = "()"
			+ Type.VOID_TYPE.getDescriptor();

	/**
	 * Initializes a part of the system. The first initialization is done in the
	 * static block. The initialization of the group communication system is
	 * deferred until the actual application is executing because, apparently,
	 * explicit class loading during the execution of an agent is only supported
	 * in JDK 7. It also initializes some things related with group and data
	 * partitioners and the distributed protocol.
	 */
	public static void init()
	{
		initGroupCommunication();

		if (PARTIAL)
		{
			groupPart.partitionGroups(getAllMembers());
			dataPart.init();

			ALL.addAll(getAllMembers()); // XXX check

			LOGGER.warn("> ALL:" + ALL); // XXX check

			System.out.println("GROUP: " + getLocalGroup().getId());
		}

		distProtocol.init();

		LOGGER.warn("> TribuDSTM initialized!");
		LOGGER.warn("####################################################");
	}

	public static final String CLOSE_METHOD_NAME = "close";
	public static final String CLOSE_METHOD_DESC = "()"
			+ Type.VOID_TYPE.getDescriptor();

	/**
	 * Closes the group communication system gracefully and terminates the
	 * system.
	 */
	public static void close()
	{
		LOGGER.warn("####################################################");
		LOGGER.warn("> TribuDSTM closing...");

		groupComm.close();

		LOGGER.warn("> TribuDSTM closed!");

		System.exit(0);
	}

	/**
	 * Initializes the group communication system component.
	 */
	private static void initGroupCommunication()
	{
		String className = System.getProperty(Defaults._COMM_CLASS,
				Defaults.COMM_CLASS);
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

		LOGGER.warn("> Group communication: " + className);
	}

	/*
	 * ################################################################
	 * ########### LOCATOR TABLE ######################################
	 * ################################################################
	 */

	/**
	 * Returns the reference to the locator table.
	 * 
	 * @return the reference to the locator table.
	 */
	public static final Locator getLocator()
	{
		return locator;
	}

	/**
	 * Returns the UniqueObject corresponding to the metadata.
	 * 
	 * @param metadata - the key to find in the locator table.
	 * @return the corresponding UniqueObject.
	 */
	public static final UniqueObject getObject(ObjectMetadata metadata)
	{
		return locator.get(metadata);
	}

	/**
	 * Puts the new entry <K,V> in the locator table, where K is metadata and V
	 * is obj.
	 * 
	 * @param metadata - the key of the new entry in the locator table.
	 * @param obj - the value of the new entry in the locator table.
	 */
	public static final void putObject(ObjectMetadata metadata, UniqueObject obj)
	{
		locator.put(metadata, obj);
	}

	/*
	 * ################################################################
	 * ########### SERIALIZATION ######################################
	 * ################################################################
	 */

	public static final String GETSERIALIZER_METHOD_NAME = "getObjectSerializer";
	public static final String GETSERIALIZER_METHOD_DESC = "()"
			+ ObjectSerializer.DESC;

	/**
	 * Returns the object serializer used by the system's distributed protocol.
	 * 
	 * @return the object serializer.
	 */
	public static final ObjectSerializer getObjectSerializer()
	{
		return distProtocol.getObjectSerializer();
	}

	/*
	 * ################################################################
	 * ########### DISTRIBUTED PROTOCOL ###############################
	 * ################################################################
	 */

	/**
	 * Returns the transaction context class used by the system.
	 * 
	 * @return the transaction context class.
	 */
	public static final Class<? extends DistributedContext> getContextClass()
	{
		return ctxClass;
	}

	/**
	 * It calls the onContextCreation callback in the system's distributed
	 * protocol.
	 * 
	 * @param ctx - the created transaction context.
	 */
	public static final void onContextCreation(DistributedContext ctx)
	{
		distProtocol.onTxContextCreation(ctx);
	}

	/**
	 * It calls the onTxBegin callback in the system's distributed protocol.
	 * 
	 * @param ctx - the transaction context of the corresponding transaction.
	 */
	public static final void onTxBegin(DistributedContext ctx)
	{
		distProtocol.onTxBegin(ctx);
	}

	/**
	 * It calls the onTxFinished callback in the system's distributed protocol.
	 * 
	 * @param ctx - the transaction context of the corresponding transaction.
	 * @param committed - true if the transaction finished successfully, false
	 *            otherwise.
	 */
	public static final void onTxFinished(DistributedContext ctx,
			boolean committed)
	{
		distProtocol.onTxFinished(ctx, committed);
	}

	/**
	 * It calls the onTxCommit callback in the system's distributed protocol.
	 * 
	 * @param ctx - the transaction context of the corresponding transaction.
	 */
	public static final void onTxCommit(DistributedContext ctx)
	{
		distProtocol.onTxCommit(ctx);
	}

	/**
	 * It calls the onTxRead callback in the system's distributed protocol.
	 * 
	 * @param ctx - the transaction context of the corresponding transaction.
	 * @param field - the TxField associated with the field being read.
	 * @return the object written in that TxField.
	 */
	public static final Object onTxRead(DistributedContext ctx, TxField field)
	{
		return distProtocol.onTxRead(ctx, field);
	}

	/*
	 * ################################################################
	 * ########### COMMUNICATION ######################################
	 * ################################################################
	 */

	/**
	 * Sends the payload totally ordered to all the cluster members.
	 * 
	 * @param payload - the byte array to be sent.
	 */
	public static final void sendTotalOrdered(byte[] payload)
	{
		groupComm.sendTotalOrdered(payload);
	}

	/**
	 * Sends the payload reliably to all the cluster members.
	 * 
	 * @param payload - the byte array to be sent.
	 */
	public static final void sendReliably(byte[] payload)
	{
		groupComm.sendReliably(payload);
	}

	/**
	 * Sends the payload totally ordered to the members of the group.
	 * 
	 * @param payload - the byte array to be sent.
	 * @param group - the group of members that will receive the message.
	 */
	public static final void sendTotalOrdered(byte[] payload, Group group)
	{
		groupComm.sendTotalOrdered(payload, group);
	}

	/**
	 * Sends the payload reliably to a specific member.
	 * 
	 * @param payload - the byte array to be sent.
	 * @param addr - the specific member that will receive the message.
	 */
	public static final void sendTo(byte[] payload, Address addr)
	{
		groupComm.sendTo(payload, addr);
	}

	/**
	 * Sends the payload reliably to the members of the group.
	 * 
	 * @param payload - the byte array to be sent.
	 * @param group - the group of members that will receive the message.
	 */
	public static final void sendToGroup(byte[] payload, Group group)
	{
		groupComm.sendToGroup(payload, group);
	}

	/**
	 * Determines if an address if local or not.
	 * 
	 * @param addr - the address to check.
	 * @return true if the address is local, false otherwise.
	 */
	public static final boolean isLocalAddress(Address addr)
	{
		return groupComm.isLocal(addr);
	}

	/**
	 * Subscribes a subscriber to receive messages from the group communication
	 * system.
	 * 
	 * @param subscriber - the subscriber that will receive the messages.
	 */
	public static final void subscribeDeliveries(DeliverySubscriber subscriber)
	{
		groupComm.subscribeDelivery(subscriber);
	}

	/**
	 * Subscribes a subscriber to receive messages from the group communication
	 * system, optimistically.
	 * 
	 * @param subscriber - the subscriber that will receive the messages,
	 *            optimistically.
	 */
	public static final void subscribeOptimisticDeliveries(
			OptimisticDeliverySubscriber subscriber)
	{
		groupComm.subscribeOptimisticDelivery(subscriber);
	}

	/**
	 * Returns a collection with all the members of the cluster. This collection
	 * is in the same order in every node.
	 * 
	 * @return - the members list.
	 */
	public static final Collection<Address> getAllMembers()
	{ // assumes members are always in the same order in
		return groupComm.getMembers(); // the collection (in every node)
	}

	/**
	 * Returns the address of this node.
	 * 
	 * @return - this node's address.
	 */
	public static final Address getLocalAddress()
	{
		return groupComm.getLocalAddress();
	}

	/*
	 * ################################################################
	 * ########### GROUPS #############################################
	 * ################################################################
	 */

	/**
	 * Returns the group where this node belongs.
	 * 
	 * @return - this node's group.
	 */
	public static final Group getLocalGroup()
	{
		return groupPart.getLocalGroup();
	}

	/**
	 * Returns a group corresponding to a specific index.
	 * 
	 * @param id - the index of the group.
	 * @return the corresponding group.
	 */
	public static final Group getGroup(int id)
	{
		return groupPart.getGroup(id);
	}

	/**
	 * Determines if a group is local or not.
	 * 
	 * @param group - the group to check.
	 * @return true if the group is local, false otherwise.
	 */
	public static final boolean isLocalGroup(Group group)
	{
		return group.contains(getLocalAddress()); // XXX use id
	}

	/**
	 * Determines if a group corresponds to the group ALL.
	 * 
	 * @param group - the group to check.
	 * @return true if the group is ALL, false otherwise.
	 */
	public static final boolean groupIsAll(Group group)
	{
		return group.getAll().equals(ALL); // XXX use id
	}

	/**
	 * Returns the number of group in the system.
	 * 
	 * @return the number of groups.
	 */
	public static final int getNumGroups()
	{
		return groupPart.getNumGroups();
	}

	/**
	 * Determines if this nodes is the master of its group.
	 * 
	 * @return true if this node is the group master, false otherwise.
	 */
	public static final boolean isGroupMaster()
	{
		Address localAddr = getLocalAddress();
		Address masterAddr = getLocalGroup().getGroupMaster();

		return localAddr.equals(masterAddr);
	}

	/*
	 * ################################################################
	 * ########### DATA ###############################################
	 * ################################################################
	 */

	/**
	 * Returns the group where the object will be replicated, according to the
	 * implemented data partitioner.
	 * 
	 * @param obj - the object to replicate.
	 * @return the group where the object will be replicated.
	 */
	public static final Group publishObjectTo(UniqueObject obj)
	{
		return dataPart.publishTo(obj);
	}

	/*
	 * ################################################################
	 * ########### CACHE ##############################################
	 * ################################################################
	 */

	public static final void cachePut(ObjectMetadata metadata, Version obj)
	{
		cache.put(metadata, obj);
	}

	public static final Version cacheGet(ObjectMetadata metadata)
	{
		return cache.get(metadata);
	}

	public static final boolean cacheContains(ObjectMetadata metadata)
	{
		return cache.contains(metadata);
	}
}
