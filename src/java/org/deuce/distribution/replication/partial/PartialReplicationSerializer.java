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
import org.deuce.transform.localmetadata.type.TxField;

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
		LOGGER.trace("----- " + obj.getClass().getSimpleName());
		
		PartialReplicationOID oid = (PartialReplicationOID) obj.getMetadata();
		boolean isRead = SCOReProtocol.serializationContext.get();

		Group toPublish = null;
		/* XXX t.vale: never happens now. */
//		if (oid == null)
//		{ // UniqueObject #metadata (it is not a TxField)
//			oid = factory.generateOID(); // creates PRepMetadata with no group
//			toPublish = TribuDSTM.publishObjectTo(obj); // choose group
//			oid.setGroup(toPublish);
//			obj.setMetadata(oid);
//
//			LOGGER.trace("< " + oid + " (oid null) "
//					+ obj.getClass().getSimpleName());
//		}
		if (isRead)
		{
			return obj;
		}
		else if (oid.isPublished())
		{
			LOGGER.trace("< " + oid + " (published) "
					+ obj.getClass().getSimpleName());
			// XXX serializar com contexto (no meu grupo(se publicado enviar
			// ids) ou não(enviar objectos))
			return new OID2Object(oid);
		}
		else
		{
			toPublish = oid.getGroup();
			if (toPublish.getAll().isEmpty())
			{ // choose group
				toPublish = TribuDSTM.publishObjectTo(obj);
				oid.getGroup().getAll().addAll(toPublish.getAll());
			}
			
			oid.publish();
			
			LOGGER.trace("< " + oid + " (oid not null) "
					+ obj.getClass().getSimpleName());
		}

		if (TribuDSTM.isLocalGroup(toPublish)) // if this is my group
		{ // save object in locator table XXX change place
			TribuDSTM.putObject(oid, obj);
		}

		return obj;
	}

	@Override
	public Object readResolveHook(UniqueObject obj)
			throws ObjectStreamException
	{
		PartialReplicationOID oid = (PartialReplicationOID) obj.getMetadata();

		Group group = oid.getGroup();
		if (TribuDSTM.isLocalGroup(group))
		{ // it is my group
			UniqueObject object = TribuDSTM.getObject(oid);
			if (object == null)
			{ // object not in locator table. put in table and return it
				LOGGER.trace("> " + oid + " (not in LT)");

				TribuDSTM.putObject(oid, obj);
				return obj;
			}
			else
			{ // object in locator table. already replicated
				LOGGER.trace("> " + oid + " (in LT)");

				return object;
			}
		}
		else
		{ // not for me to replicate this object
			LOGGER.trace("> " + oid + " (no rep)");
			
			if (obj instanceof TxField)
			{
				TribuDSTM.putObject(oid, obj);
			}

			return obj;
		}
	}

	@Override
	public ObjectMetadata createMetadata()
	{ // Generates an empty metadata (group is null)
		return factory.generateOID();
	}

	public static final String CREATE_PARTIAL_METADATA_METHOD_NAME = "createPartialReplicationMetadata";
	public static final String CREATE_PARTIAL_METADATA_METHOD_DESC = "("
			+ UniqueObject.DESC + ")" + Type.VOID_TYPE.getDescriptor();

	public void createPartialReplicationMetadata(UniqueObject obj)
	{ // Generates an empty metadata (group is null)
		ObjectMetadata meta = factory.generateOID();
		obj.setMetadata(meta);
		TribuDSTM.putObject(meta, obj); // XXX
	}

	public static final String CREATE_FULL_METADATA_METHOD_NAME = "createFullReplicationMetadata";
	public static final String CREATE_FULL_METADATA_METHOD_DESC = "("
			+ UniqueObject.DESC + ")" + Type.VOID_TYPE.getDescriptor();

	public void createFullReplicationMetadata(UniqueObject obj)
	{ // Generates metadata with the group assigned with ALL
		ObjectMetadata meta = factory.generateFullReplicationOID();
		obj.setMetadata(meta);
		TribuDSTM.putObject(meta, obj); // XXX
	}

	public static final String BOOTSTRAP_METHOD_NAME = "createBootstrapOID";
	public static final String BOOTSTRAP_METHOD_DESC = "(" + UniqueObject.DESC
			+ Type.INT_TYPE.getDescriptor() + ")"
			+ Type.VOID_TYPE.getDescriptor();

	public void createBootstrapOID(UniqueObject obj, int id)
	{
		PartialReplicationOID meta = factory.generateFullReplicationOID(id);
		obj.setMetadata(meta);
		meta.publish();
		TribuDSTM.putObject(meta, obj);

		LOGGER.info(String.format("! BootstrapOID %s id(%d) = %s", obj, id,
				meta));
	}
}
