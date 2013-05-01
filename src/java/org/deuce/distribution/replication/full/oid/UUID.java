package org.deuce.distribution.replication.full.oid;

import java.nio.ByteBuffer;

import org.deuce.distribution.replication.full.OID;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class UUID implements OID
{
	private static final long serialVersionUID = 3858627429701566285L;
	private java.util.UUID uuid;

	protected UUID()
	{
		uuid = java.util.UUID.randomUUID();
	}

	protected UUID(int seed)
	{
		byte[] name = ByteBuffer.allocate(4).putInt(seed).array();
		uuid = java.util.UUID.nameUUIDFromBytes(name);
	}

	@Override
	public boolean equals(Object obj)
	{
		return obj instanceof UUID && uuid.equals(((UUID) obj).uuid);
	}

	@Override
	public int hashCode()
	{
		return uuid.hashCode();
	}

	@Override
	public String toString()
	{
		return uuid.toString();
	}
}
