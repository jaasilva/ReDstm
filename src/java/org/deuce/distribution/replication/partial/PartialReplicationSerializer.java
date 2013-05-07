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
import org.deuce.distribution.replication.partial.oid.PartialReplicationOID;
import org.deuce.distribution.replication.partial.oid.PartialReplicationOIDFactory;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
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

	@Override
	public Object writeReplaceHook(UniqueObject obj)
			throws ObjectStreamException
	{
		PartialReplicationOID oid = (PartialReplicationOID) obj.getMetadata();

		if (oid == null)
		{ // XXX does this happens???
			oid = factory.generateOID(); // creates PRepMetadata with no group
			oid.generateId(); // assign id
			Group toPublish = TribuDSTM.publishObjectTo(obj); // chooses group
			oid.setGroup(toPublish);
			obj.setMetadata(oid);

			if (TribuDSTM.isLocalGroup(toPublish)) // if this is my group
			{ // save object in locator table
				TribuDSTM.putObject(oid, obj);
			}

			LOGGER.debug(String.format("Published %s with OID(%s)", obj, oid));

			return obj;
		}
		else if (!oid.isIdAssigned())
		{ // id not assigned. object not published
			oid.generateId();

			Group toPublish = oid.getGroup();
			if (toPublish == null)
			{ // chooses group
				toPublish = TribuDSTM.publishObjectTo(obj);
				oid.setGroup(toPublish);
			}

			if (TribuDSTM.isLocalGroup(toPublish)) // if this is my group
			{ // save object in locator table
				TribuDSTM.putObject(oid, obj);
			}

			LOGGER.debug(String.format("Published %s with OID(%s)", obj, oid));

			return obj;
		}
		else
		{ // id assigned. object already published. send stub instead
			LOGGER.debug(String.format("%s already published with OID(%s)",
					obj, oid));

			return new OID2Object(oid);
		}
	}

	@Override
	public Object readResolveHook(UniqueObject obj)
			throws ObjectStreamException
	{
		PartialReplicationOID oid = (PartialReplicationOID) obj.getMetadata();
		Group group = oid.getGroup();

		if (TribuDSTM.getLocalGroup().equals(group))
		{ // it is my group
			UniqueObject object = TribuDSTM.getObject(oid);
			if (object == null)
			{ // object not in locator table. put in table and return it
				TribuDSTM.putObject(oid, obj);

				LOGGER.debug(String.format("Freshly published %s with OID(%s)",
						obj, oid));

				return obj;
			}
			else
			{ // object in locator table. already replicated
				LOGGER.debug(String.format(
						"Replaced %s with OID(%s) by local replica %s", obj,
						oid, object));

				return object;
			}
		}
		else
		{ // not for me to replicate this object
			return obj;
		}
	}

	@Override
	public ObjectMetadata createMetadata()
	{ // Generates an empty metadata (id and group are null)
		return factory.generateOID();
	}

	public static final String CREATE_PARTIAL_METADATA_METHOD_NAME = "createPartialReplicationMetadata";
	public static final String CREATE_PARTIAL_METADATA_METHOD_DESC = "("
			+ UniqueObject.DESC + ")" + Type.VOID_TYPE.getDescriptor();

	public void createPartialReplicationMetadata(UniqueObject obj)
	{ // Generates an empty metadata (id and group are null)
		ObjectMetadata meta = factory.generateOID();
		obj.setMetadata(meta);
	}

	public static final String CREATE_FULL_METADATA_METHOD_NAME = "createFullReplicationMetadata";
	public static final String CREATE_FULL_METADATA_METHOD_DESC = "("
			+ UniqueObject.DESC + ")" + Type.VOID_TYPE.getDescriptor();

	public void createFullReplicationMetadata(UniqueObject obj)
	{ // Generates metadata with the group assigned with ALL (id is null)
		ObjectMetadata meta = factory.generateFullReplicationOID();
		obj.setMetadata(meta);
	}

	public static final String BOOTSTRAP_METHOD_NAME = "createBootstrapOID";
	public static final String BOOTSTRAP_METHOD_DESC = "(" + UniqueObject.DESC
			+ Type.INT_TYPE.getDescriptor() + ")"
			+ Type.VOID_TYPE.getDescriptor();

	public void createBootstrapOID(UniqueObject obj, int id)
	{
		PartialReplicationOID oid = factory.generateFullReplicationOID(id);
		obj.setMetadata(oid);
		TribuDSTM.putObject(oid, obj);

		LOGGER.debug(String.format(
				"Created bootstrapOID for %s with id(%d) = %s", obj, id, oid));
	}
}
