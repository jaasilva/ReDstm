package org.deuce.transaction.score2.field;

import org.deuce.reflection.AddressUtil;
import org.deuce.reflection.UnsafeHolder;
import org.deuce.transaction.score2.LockTable;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public class VBoxFieldS extends TxField implements VBoxS
{
	public VersionS version;

	public VBoxFieldS(Object ref, long address)
	{
		super(ref, address);
		version = new VersionS(0, readShort(), null);
	}

	public boolean validate(Version version, int owner)
	{
		Version tmp = this.version;
		int l = lock;
		if ((l & LockTable.LOCK) != 0)
		{
			if ((l & LockTable.UNLOCK) != owner)
			{
				throw LockTable.LOCKED_VERSION_EXCEPTION;
			}
		}
		return tmp == version;
	}

	public void commit(short newValue, int txNumber)
	{
		VersionS ver = new VersionS(Integer.MAX_VALUE, newValue, version);
		this.version.value = readShort();
		this.version = ver;
		writeShort(ver.value);
		this.version.version = txNumber;
	}

	@Override
	public Version get(int version)
	{
		if ((lock & LockTable.LOCK) != 0)
		{
			throw LockTable.LOCKED_VERSION_EXCEPTION;
		}
		return this.version.get(version);
	}

	private static long __LOCK_FIELD__;
	static
	{
		try
		{
			__LOCK_FIELD__ = AddressUtil.getAddress(VBoxFieldS.class
					.getDeclaredField("lock"));
		}
		catch (SecurityException e)
		{
		}
		catch (NoSuchFieldException e)
		{
		}
	}
	public volatile int lock = 0;

	@Override
	public boolean lock(int owner)
	{
		int l = lock;
		if ((l & LockTable.LOCK) != 0)
		{
			throw LockTable.LOCKED_VERSION_EXCEPTION;
		}
		if (!UnsafeHolder.getUnsafe().compareAndSwapInt(this, __LOCK_FIELD__,
				l, l | owner | LockTable.LOCK))
		{
			throw LockTable.LOCKED_VERSION_EXCEPTION;
		}
		return true;
	}

	@Override
	public void unLock()
	{
		lock = 0;
	}

	@Override
	public Version getTop()
	{
		return version;
	}
}
