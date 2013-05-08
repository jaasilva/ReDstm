package org.deuce.transaction.score.field;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.deuce.objectweb.asm.Type;
import org.deuce.transaction.score.InPlaceRWLock;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class VBoxField extends TxField implements InPlaceRWLock
{
	public final static String NAME = Type.getInternalName(VBoxField.class);

	public Version version;
	public int type;
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

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
		// this.version.value = read(type);
		this.version = ver;
		// write(ver.value, type);
		this.version.version = sid;
	}

	public Version get(int maxVersion)
	{
		return this.version.get(maxVersion);
	}

	public Version getLastVersion()
	{
		return version;
	}

	@Override
	public boolean exclusiveLock()
	{
		return lock.writeLock().tryLock();
	}

	@Override
	public void exclusiveUnlock()
	{
		lock.writeLock().unlock();
	}

	@Override
	public boolean sharedLock()
	{
		return lock.readLock().tryLock();
	}

	@Override
	public void sharedUnlock()
	{
		lock.readLock().unlock();
	}

	@Override
	public boolean isExclusiveUnlocked()
	{
		return !lock.isWriteLocked();
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
		}
		return null;
	}
}
