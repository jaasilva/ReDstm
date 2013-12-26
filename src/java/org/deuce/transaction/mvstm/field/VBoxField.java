package org.deuce.transaction.mvstm.field;

import org.deuce.objectweb.asm.Type;
import org.deuce.reflection.AddressUtil;
import org.deuce.reflection.UnsafeHolder;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.mvstm.Context;
import org.deuce.transaction.mvstm.LockTable;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ArrayContainer;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 */
@ExcludeTM
public class VBoxField extends TxField implements InPlaceLock,
		java.io.Serializable
{
	public final static String NAME = Type.getInternalName(VBoxField.class);

	public volatile Version version;
	public int type;

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

	public void commit(Object newVal, int txNumber)
	{
		Version ver = new Version(Integer.MAX_VALUE, newVal, version);
		this.version.value = read(type); // *WEAK ISOLATION*
		this.version = ver;
		write(ver.value, type); // *WEAK ISOLATION*
		this.version.version = txNumber;
	}

	public boolean validate(Version version, DistributedContext owner)
	{
		Version tmp = this.version;
		int l = lock;
		Context lh = lockHolder;

		if (((l & LockTable.LOCK) != 0) && (lh != (Context) owner))
		{
			throw LockTable.LOCKED_VERSION_EXCEPTION;
		}
		return tmp.equals(version); // XXX equals ???
	}

	public Version get(int maxVersion)
	{
		if ((lock & LockTable.LOCK) != 0)
		{
			throw LockTable.LOCKED_VERSION_EXCEPTION;
		}
		return this.version.get(maxVersion);
	}

	private static long __LOCK_FIELD__;
	static
	{
		try
		{
			__LOCK_FIELD__ = AddressUtil.getAddress(VBoxField.class
					.getDeclaredField("lock"));
		}
		catch (Exception e)
		{
		}
	}
	public volatile int lock = 0;
	private transient volatile Context lockHolder; // Object reference

	public Version getLastVersion()
	{
		return this.version;
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

	@Override
	public boolean lock(DistributedContext locker)
	{
		int l = lock;

		if ((l & LockTable.LOCK) != 0)
		{
			throw LockTable.LOCKED_VERSION_EXCEPTION;
		}

		boolean isLocked = UnsafeHolder.getUnsafe().compareAndSwapInt(this,
				__LOCK_FIELD__, l, l | LockTable.LOCK);

		if (!isLocked)
		{
			throw LockTable.LOCKED_VERSION_EXCEPTION;
		}

		lockHolder = (Context) locker; // mark as "locked by"

		return true;
	}

	@Override
	public void unLock()
	{
		lockHolder = null;
		lock = lock & LockTable.UNLOCK;
	}
}
