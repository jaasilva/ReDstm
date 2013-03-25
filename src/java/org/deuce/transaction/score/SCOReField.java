package org.deuce.transaction.score;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ArrayContainer;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class SCOReField extends TxField implements Serializable
{
	@ExcludeTM
	static public enum Type
	{
		BYTE, BOOLEAN, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, OBJECT, ARRAY
	}

	private Version version;
	private Type type;

	/**
	 * 
	 */
	public SCOReField()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ref
	 * @param address
	 */
	public SCOReField(Object ref, long address, Type type)
	{
		super(ref, address);
		this.type = type;
		version = new Version(getValue(), 0, null);
	}

	/**
	 * @param containersRef
	 * @param idx
	 * @param backend
	 */
	public SCOReField(Object[] containersRef, int idx, Object[] backend,
			Type type)
	{
		super(containersRef, idx, backend);
		this.type = type;
		version = new Version(getValue(), 0, null);
	}

	public Object getValue()
	{
		switch (type)
		{
			case BYTE:
				return super.readByte();
			case BOOLEAN:
				return super.readBoolean();
			case CHAR:
				return super.readChar();
			case SHORT:
				return super.readShort();
			case INT:
				return super.readInt();
			case LONG:
				return super.readLong();
			case FLOAT:
				return super.readFloat();
			case DOUBLE:
				return super.readDouble();
			case OBJECT:
				return super.readObject();
			case ARRAY:
				return super.readArray();
		}
		return null;
	}

	public void putValue(Object value)
	{
		switch (type)
		{
			case BYTE:
				super.writeByte((Byte) value);
			case BOOLEAN:
				super.writeBoolean((Boolean) value);
			case CHAR:
				super.writeChar((Character) value);
			case SHORT:
				super.writeShort((Short) value);
			case INT:
				super.writeInt((Integer) value);
			case LONG:
				super.writeLong((Long) value);
			case FLOAT:
				super.writeFloat((Float) value);
			case DOUBLE:
				super.writeDouble((Double) value);
			case OBJECT:
				super.writeObject(value);
			case ARRAY:
				super.writeArray((ArrayContainer) value);
		}
	}
}
