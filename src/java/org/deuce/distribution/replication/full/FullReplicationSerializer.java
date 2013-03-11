package org.deuce.distribution.replication.full;

import java.io.ObjectStreamException;

import org.apache.log4j.Logger;
import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.OID;
import org.deuce.distribution.replication.OIDFactory;
import org.deuce.distribution.replication.full.oid.UUIDFactory;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class FullReplicationSerializer extends ObjectSerializer
{
	private static final Logger LOGGER = Logger.getLogger(FullReplicationSerializer.class);
	public final static String NAME = Type
			.getInternalName(FullReplicationSerializer.class);
	public final static String DESC = Type
			.getDescriptor(FullReplicationSerializer.class);

	// TODO: Permitir definir qual a implementação de OID a usar.
	// private OIDFactory factory = new SimpleOIDFactory();
	private OIDFactory factory = new UUIDFactory();

	/**
	 * If this object has already been published, we serialise a replacement
	 * object that, when de-serialised on the target host, returns the target's
	 * local copy of this object. Otherwise, it initialises this object's OID,
	 * adds the object to the global table and serialises normally.
	 * 
	 * @return UniqueObjectMapper iff this object has already been published,
	 *         otherwise returns this object.
	 * @throws ObjectStreamException
	 */
	public Object writeReplaceHook(UniqueObject obj)
			throws ObjectStreamException
	{
		OID oid = (OID) obj.getMetadata();

		if (oid == null)
		{ // not published
			oid = factory.generateOID();
			obj.setMetadata(oid);
			TribuDSTM.putObject(oid, obj);
			LOGGER.trace(String.format("Published %s with OID(%s)",
					obj.toString(), oid.toString()));
			return obj;
		}
		else
		{
			LOGGER.trace(String.format("%s already published with OID(%s)",
					obj.toString(), oid.toString()));
			return new OID2Object(oid);
		}
	}

	/**
	 * If there already exists a local copy of this object, return it. Otherwise
	 * it is freshly published, we add it to the global table and return it.
	 * 
	 * @throws ObjectStreamException
	 */
	public Object readResolveHook(UniqueObject obj)
			throws ObjectStreamException
	{
		OID oid = (OID) obj.getMetadata();

		UniqueObject object = TribuDSTM.getObject(oid);
		if (object != null)
		{
			LOGGER.trace(String.format(
					"Replaced %s with OID(%s) by local replica %s",
					obj.toString(), oid.toString(), object.toString()));
			return object;
		}
		else
		{
			TribuDSTM.putObject(oid, obj);
			LOGGER.trace(String.format("Published %s with OID(%s)",
					obj.toString(), oid.toString()));
			return obj;
		}
	}

	public ObjectMetadata createMetadata()
	{
		return factory.generateOID();
	}

	public static final String BOOTSTRAP_METHOD_NAME = "createBootstrapOID";
	public static final String BOOTSTRAP_METHOD_DESC = "(" + UniqueObject.DESC
			+ Type.INT_TYPE.getDescriptor() + ")"
			+ Type.VOID_TYPE.getDescriptor();

	public void createBootstrapOID(UniqueObject obj, int id)
	{
		OID oid = factory.generateOID(id);
		obj.setMetadata(oid);
		TribuDSTM.putObject(oid, obj);
	}
}
