package org.deuce.transaction.score2.field;

import org.deuce.reflection.AddressUtil;
import org.deuce.reflection.UnsafeHolder;
import org.deuce.transaction.score2.LockTable;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>, jaasilva
 */
@ExcludeTM
public class VBoxField extends TxField implements VBox
{
	public Version version;
	final private Type type;

	public VBoxField(Object ref, long address, Type type)
	{
		super(ref, address);
		this.type = type;
		version = new Version(0, read(type), null);
	}

	public VBoxField(Object[] arr, int idx, Type type)
	{ // used for unidimensional arrays
		super(arr, idx, null); // XXX o que meter no backend?
		this.type = type;
		version = new Version(0, read(type), null);
	}

	// XXX multiarrays como é?

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

	public void commit(Object newValue, int txNumber)
	{
		Version ver = new Version(Integer.MAX_VALUE, newValue, version);
		this.version.value = read(type);
		this.version = ver;
		write(ver.value, type);
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
			__LOCK_FIELD__ = AddressUtil.getAddress(VBoxField.class
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
