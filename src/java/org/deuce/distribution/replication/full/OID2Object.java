package org.deuce.distribution.replication.full;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

/**
 * This class acts as an replacement serialised version of an already published
 * unique object. Therefore, upon de-serialisation on the target host, it
 * returns the existing local copy of the unique object.
 * 
 * @author Tiago Vale
 */
@ExcludeTM
public final class OID2Object implements Serializable
{
	private static final Logger LOGGER = Logger.getLogger(OID2Object.class);
	private static final long serialVersionUID = 1L;
	public static final String DESC = Type.getDescriptor(OID2Object.class);
	public static final String NAME = Type.getInternalName(OID2Object.class);

	public static final String OID_FIELD_NAME = "oid";
	public final OID oid;

	public static final String CTOR_DESC = "(" + OID.DESC + ")V";

	public OID2Object(OID oid)
	{
		this.oid = oid;
	}

	/**
	 * If we are de-serialising an object of this class it means there is a
	 * local copy of the object with this OID. We therefore return the local
	 * copy of the unique object.
	 * 
	 * @return Local copy of the unique object.
	 * @throws ObjectStreamException
	 */
	protected Object readResolve() throws ObjectStreamException
	{
		UniqueObject object = TribuDSTM.getObject(oid);
		if (object == null)
		{
			LOGGER.fatal(String.format(
					"RECEIVED OID(%s) WITHOUT LOCAL REPLICA", oid.toString()));
		}
		else
		{
			LOGGER.trace(String.format(
					"Replaced OID(%s) with local replica %s", oid.toString(),
					object.toString()));
		}
		return object;
	}
}
