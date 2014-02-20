package org.deuce.distribution.replication.partial;

import java.io.ObjectStreamException;

import org.apache.log4j.Logger;
import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.OID2Object;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.oid.PartialReplicationMetadataFactory;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class PartialReplicationSerializer extends ObjectSerializer
{
	private static final Logger LOGGER = Logger
			.getLogger(PartialReplicationSerializer.class);
	public final static String NAME = Type
			.getInternalName(PartialReplicationSerializer.class);
	public final static String DESC = Type
			.getDescriptor(PartialReplicationSerializer.class);

	private PartialReplicationOIDFactory factory = new PartialReplicationMetadataFactory();

	/**
	 * In a read context I always have to send the object. Otherwise, if the
	 * object is already published I can send a stub. If the object is not
	 * published I publish the object and send it.
	 * 
	 * @return the object.
	 * @throws ObjectStreamException
	 */
	@Override
	public Object writeReplaceHook(UniqueObject obj)
			throws ObjectStreamException
	{
		PartialReplicationOID oid = (PartialReplicationOID) obj.getMetadata();
		boolean isRead = PartialReplicationProtocol.serializationReadCtx.get();

		if (isRead)
		{ // read context. I have to send the object
			return obj;
		}
		else if (oid.isPublished())
		{ // already published. send stub instead
			return new OID2Object(oid);
		}
		else
		{ // publish object and send
			Group group = oid.getGroup();
			oid.publish();
			if (group.isLocal())
			{ // if this is my group save object in locator table
				TribuDSTM.putObject(oid, obj);
			}
			return obj; // return the object itself
		}
	}

	/**
	 * If the group of the object is local I have to check my locator table for
	 * a local copy. If I have a copy of the object in the locator table I
	 * return it, otherwise I save it in my locator table and return the
	 * received object. If the group is not local I just return the object (I
	 * save the object in the locator table just to cache the received object
	 * graph).
	 * 
	 * @return the object.
	 * @throws ObjectStreamException
	 */
	@Override
	public Object readResolveHook(UniqueObject obj)
			throws ObjectStreamException
	{
		PartialReplicationOID oid = (PartialReplicationOID) obj.getMetadata();
		Group group = oid.getGroup();

		boolean isLocalGroup = true; // XXX why?
		try
		{
			isLocalGroup = group.isLocal();
		}
		catch (NullPointerException e)
		{ // XXX why does this happen?
		}

		if (isLocalGroup)
		{ // it is my group
			UniqueObject object = TribuDSTM.getObject(oid);

			if (object == null)
			{ // object not in locator table. put in table and return it
				TribuDSTM.putObject(oid, obj);
				return obj;
			}
			else
			{ // object in locator table. already replicated
				return object; // return local object
			}
		}
		else
		{ // not for me to replicate this object
			/*
			 * to cache the object graph received from a read request during the
			 * transaction
			 */
			TribuDSTM.putObject(oid, obj);
			return obj;
		}
	}

	@Override
	public ObjectMetadata createMetadata()
	{ // id = rand(), group = ALL, partialGroup = []
		return factory.generateOID();
	}

	public static final String CREATE_PARTIAL_METADATA_METHOD_NAME = "createPartialReplicationMetadata";
	public static final String CREATE_PARTIAL_METADATA_METHOD_DESC = "("
			+ UniqueObject.DESC + ")" + Type.VOID_TYPE.getDescriptor();

	/**
	 * Creates a new partial replication metadata object.
	 * 
	 * @param obj - the UniqueObject that will be associated with the newly
	 *            created metadata.
	 */
	public void createPartialReplicationMetadata(UniqueObject obj)
	{ // id = rand(), group = ALL, partialGroup = [toPublish]
		ObjectMetadata meta = factory.generateOID();
		obj.setMetadata(meta);
		final Group toPublish = TribuDSTM.publishObjectTo(obj);
		((PartialReplicationOID) meta).getPartialGroup().getAll()
				.addAll(toPublish.getAll()); // XXX find better way!!!
		TribuDSTM.putObject(meta, obj); // XXX check
	}

	public static final String CREATE_FULL_METADATA_METHOD_NAME = "createFullReplicationMetadata";
	public static final String CREATE_FULL_METADATA_METHOD_DESC = "("
			+ UniqueObject.DESC + ")" + Type.VOID_TYPE.getDescriptor();

	/**
	 * Creates a new full replication metadata object.
	 * 
	 * @param obj - the UniqueObject that will be associated with the newly
	 *            created metadata.
	 */
	public void createFullReplicationMetadata(UniqueObject obj)
	{ // id = rand(), group = partialGroup = ALL
		ObjectMetadata meta = factory.generateFullReplicationOID();
		obj.setMetadata(meta);
		TribuDSTM.putObject(meta, obj); // XXX check
	}

	public static final String BOOTSTRAP_METHOD_NAME = "createBootstrapMetadata";
	public static final String BOOTSTRAP_METHOD_DESC = "(" + UniqueObject.DESC
			+ Type.INT_TYPE.getDescriptor() + ")"
			+ Type.VOID_TYPE.getDescriptor();

	/**
	 * Creates a new bootstrap (full replication) metadata object.
	 * 
	 * @param obj - the UniqueObject that will be associated with the newly
	 *            created metadata.
	 * @param id - the id to force the UniqueObject id to be deterministic.
	 */
	public void createBootstrapMetadata(UniqueObject obj, int id)
	{ // id = rand(oid), group = partialGroup = ALL
		PartialReplicationOID meta = factory.generateFullReplicationOID(id);
		obj.setMetadata(meta);
		meta.publish(); // bootstrap objects are created already published
		TribuDSTM.putObject(meta, obj);
	}
}
