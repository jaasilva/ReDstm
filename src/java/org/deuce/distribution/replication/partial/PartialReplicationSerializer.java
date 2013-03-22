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

	// CHECKME o utlizador poder parametrizar esta factory
	private PartialReplicationOIDFactory factory = new PartialReplicationMetadataFactory();

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.ObjectSerializer#writeReplaceHook(org.deuce.
	 * distribution.UniqueObject)
	 */
	@Override
	public Object writeReplaceHook(UniqueObject obj)
			throws ObjectStreamException
	{
		PartialReplicationOID oid = (PartialReplicationOID) obj.getMetadata();

		if (oid == null)
		{ // object not published
			// CHECKME se obj for para ser full replicated como é?
			oid = factory.generateOID(); // creates PRepMetadata with no group
			Group toPublish = TribuDSTM.publishObjectTo(obj); // chooses group
			oid.setGroup(toPublish);
			obj.setMetadata(oid);

			LOGGER.trace(String.format("Published %s with OID(%s)",
					obj.toString(), oid.toString()));
			if (TribuDSTM.isLocalGroup(toPublish)) // if this is my group
			{ // save object in locator table
				TribuDSTM.putObject(oid, obj);
			}
			return obj;
		}
		else
		{ // object already published. Send stub instead
			LOGGER.trace(String.format("%s already published with OID(%s)",
					obj.toString(), oid.toString()));
			return new OID2Object(oid);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.ObjectSerializer#readResolveHook(org.deuce.
	 * distribution.UniqueObject)
	 */
	@Override
	public Object readResolveHook(UniqueObject obj)
			throws ObjectStreamException
	{ // CHECKME esta linha só funca se só forem usados PRepMetadata
		PartialReplicationOID oid = (PartialReplicationOID) obj.getMetadata();

		UniqueObject object = TribuDSTM.getObject(oid);
		if (object != null)
		{ // exists local object
			LOGGER.trace(String.format(
					"Replaced %s with OID(%s) by local replica %s",
					obj.toString(), oid.toString(), object.toString()));
			return object;
		}
		else
		{ // new received object
			TribuDSTM.putObject(oid, obj);
			LOGGER.trace(String.format("Freshly published %s with OID(%s)",
					obj.toString(), oid.toString()));
			return obj;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.distribution.ObjectSerializer#createMetadata()
	 */
	@Override
	public ObjectMetadata createMetadata()
	{ // CHECKME ver se isto chega. este metadado nao tem grupo definido
		return factory.generateOID();
	}

	public static final String BOOTSTRAP_METHOD_NAME = "createBootstrapOID";
	public static final String BOOTSTRAP_METHOD_DESC = "(" + UniqueObject.DESC
			+ Type.INT_TYPE.getDescriptor() + ")"
			+ Type.VOID_TYPE.getDescriptor();

	public void createBootstrapOID(UniqueObject obj, int id)
	{ // CHECKME por agora fica assim. mas preciso de ver melhor isto
		// se obj for estrutura é para ser replicado totalmente
		// se obj for dados é para ser replicado parcialmente
		PartialReplicationOID oid = factory.generateFullReplicationOID(id);
		obj.setMetadata(oid);
		TribuDSTM.putObject(oid, obj);
	}
}
