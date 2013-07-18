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
import org.deuce.distribution.replication.partial.protocol.score.SCOReProtocol;
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
		boolean isRead = SCOReProtocol.serializationContext.get();

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
		}

		return obj;
	}

	@Override
	public Object readResolveHook(UniqueObject obj)
			throws ObjectStreamException
	{
		PartialReplicationOID oid = (PartialReplicationOID) obj.getMetadata();
		Group group = oid.getGroup();

		if (group.isLocal())
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
			TribuDSTM.putObject(oid, obj); // to cache the object graph
			// received from a read request during a transaction
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

	public void createPartialReplicationMetadata(UniqueObject obj)
	{ // id = rand(), group = ALL, partialGroup = [toPublish]
		ObjectMetadata meta = factory.generateOID();
		obj.setMetadata(meta);
		final Group toPublish = TribuDSTM.publishObjectTo(obj);
		((PartialReplicationOID) meta).getPartialGroup().getAll()
				.addAll(toPublish.getAll());
		TribuDSTM.putObject(meta, obj); // XXX
	}

	public static final String CREATE_FULL_METADATA_METHOD_NAME = "createFullReplicationMetadata";
	public static final String CREATE_FULL_METADATA_METHOD_DESC = "("
			+ UniqueObject.DESC + ")" + Type.VOID_TYPE.getDescriptor();

	public void createFullReplicationMetadata(UniqueObject obj)
	{ // id = rand(), group = partialGroup = ALL
		ObjectMetadata meta = factory.generateFullReplicationOID();
		obj.setMetadata(meta);
		TribuDSTM.putObject(meta, obj); // XXX
	}

	public static final String BOOTSTRAP_METHOD_NAME = "createBootstrapOID";
	public static final String BOOTSTRAP_METHOD_DESC = "(" + UniqueObject.DESC
			+ Type.INT_TYPE.getDescriptor() + ")"
			+ Type.VOID_TYPE.getDescriptor();

	public void createBootstrapOID(UniqueObject obj, int id)
	{ // id = rand(oid), group = partialGroup = ALL
		PartialReplicationOID meta = factory.generateFullReplicationOID(id);
		obj.setMetadata(meta);
		meta.publish(); // bootstrap objects are created already published
		TribuDSTM.putObject(meta, obj);

		LOGGER.info(String.format("@Bootstrap(%d) = %s", id, meta));
	}
}
