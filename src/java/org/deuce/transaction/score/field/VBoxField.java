package org.deuce.transaction.score.field;

import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ArrayContainer;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 */
@ExcludeTM
public class VBoxField extends TxField implements InPlaceRWLock,
		java.io.Serializable
{
	public final static String NAME = Type.getInternalName(VBoxField.class);

	public volatile Version version;
	public int type;

	private transient volatile int readLock = 0;
	private transient volatile boolean writeLock = false; // false -> unlocked
	private transient volatile String lockHolder = null; // trxID

	public VBoxField()
	{
	}

	@Override
	public void init(Object ref, long address)
	{
		super.init(ref, address);
	}

	public VBoxField(Object ref, long address)
	{
		super(ref, address);
	}

	public static final String SET_TYPE_METHOD_NAME = "setType";
	public static final String SET_TYPE_METHOD_DESC = "("
			+ Type.INT_TYPE.getDescriptor() + ")"
			+ Type.VOID_TYPE.getDescriptor();

	public void setType(int type)
	{
		this.type = type;
		version = new Version(0, read(type), null);
	}

	public void commit(Object newVal, int sid)
	{
		Version ver = new Version(Integer.MAX_VALUE, newVal, version);
		this.version.value = read(type); // *WEAK ISOLATION*
		this.version = ver;
		write(ver.value, type); // *WEAK ISOLATION*
		this.version.version = sid;
	}

	public Version get(int maxVersion)
	{
		return this.version.get(maxVersion);
	}

	public Version getLastVersion()
	{
		return this.version;
	}

	@Override
	public boolean exclusiveLock(String holder)
	{
		if (readLock != 0)
		{ // readLock LOCKED
			return false;
		}
		else if (!writeLock)
		{ // readLock UNLOCKED && writeLock UNLOCKED
			writeLock = true;
			lockHolder = holder;
			return true;
		}
		// readLock UNLOCKED && writeLock LOCKED
		return false;
	}

	@Override
	public boolean exclusiveUnlock(String holder)
	{
		if (!holder.equals(lockHolder))
		{ // lockHolder != holder
			return false;
		}
		else if (writeLock)
		{ // lockHolder == holder && writeLock LOCKED
			writeLock = false;
			lockHolder = null; // clean lockHolder
			return true;
		}
		// lockHolder == holder && writeLock UNLOCKED
		return false;
	}

	@Override
	public boolean sharedLock(String holder)
	{
		if (writeLock && !holder.equals(lockHolder))
		{ // writeLock LOCKED && lockHolder != holder
			return false;
		}
		else
		{ // writeLock UNLOCKED (&& lockHolder == holder)
			readLock++;
			return true;
		}
	}

	@Override
	public boolean sharedUnlock(String holder)
	{ // USE CAREFULY!!! everyone can call this method without any check
		if (readLock != 0)
		{ // readLock LOCKED
			readLock--;
			return true;
		}
		// readLock UNLOCKED
		return false;
	}

	@Override
	public boolean isExclusiveUnlocked()
	{
		return !writeLock;
	}

	public void write(Object value, int type)
	{
		switch (type)
		{
		case Type.BYTE:
			writeByte((Byte) value);
			break;
		case Type.BOOLEAN:
			writeBoolean((Boolean) value);
			break;
		case Type.CHAR:
			writeChar((Character) value);
			break;
		case Type.SHORT:
			writeShort((Short) value);
			break;
		case Type.INT:
			writeInt((Integer) value);
			break;
		case Type.LONG:
			writeLong((Long) value);
			break;
		case Type.FLOAT:
			writeFloat((Float) value);
			break;
		case Type.DOUBLE:
			writeDouble((Double) value);
			break;
		case Type.OBJECT:
			writeObject(value);
			break;
		case Type.ARRAY:
			writeArray((ArrayContainer) value);
		}
	}

	public Object read(int type)
	{
		switch (type)
		{
		case Type.BYTE:
			return readByte();
		case Type.BOOLEAN:
			return readBoolean();
		case Type.CHAR:
			return readChar();
		case Type.SHORT:
			return readShort();
		case Type.INT:
			return readInt();
		case Type.LONG:
			return readLong();
		case Type.FLOAT:
			return readFloat();
		case Type.DOUBLE:
			return readDouble();
		case Type.OBJECT:
			return readObject();
		case Type.ARRAY:
			return readArray();
		}
		return null;
	}
}
