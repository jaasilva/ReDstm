package org.deuce.transaction.score.field;

import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.deuce.transaction.score.InPlaceRWLock;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>, jaasilva
 */
@ExcludeTM
public class VBoxField extends TxField implements InPlaceRWLock
{
	@ExcludeTM
	static public enum __Type
	{
		BYTE, BOOLEAN, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, OBJECT
	}

	public Version version;
	public __Type type;
	private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();

	public VBoxField()
	{
	}
	
	public VBoxField(Object ref, long address, __Type type)
	{
		super(ref, address);
		this.type = type;
		version = new Version(0, read(type), null);
	}

	public VBoxField(Object[] arr, int idx, __Type type)
	{ // used for unidimensional arrays
		super(arr, idx, null); // XXX o que meter no backend?
		this.type = type;
		version = new Version(0, read(type), null);
	}

	// XXX multiarrays como é?

//	public boolean validate(Version version, int owner)
//	{
//		Version tmp = this.version;
//		// int l = lock;
//		// if ((l & LockTable.LOCK) != 0)
//		// {
//		// if ((l & LockTable.UNLOCK) != owner) // está locked e nao sou eu que
//		// // tenho o lock?????????
//		// {
//		// throw LockTable.LOCKED_VERSION_EXCEPTION;
//		// }
//		// }
//		// TODO
//		return tmp == version;
//	}

	public void commit(Object newValue, int txNumber)
	{
		Version ver = new Version(Integer.MAX_VALUE, newValue, version);
		this.version.value = read(type); // CHECKME pq isto?
		this.version = ver;
		write(ver.value, type);
		this.version.version = txNumber;
	}

	public Version get(int version)
	{
		// if ((lock & LockTable.LOCK) != 0) // esta locked????????'
		// {
		// throw LockTable.LOCKED_VERSION_EXCEPTION;
		// }
		// TODO
		return this.version.get(version);
	}

	public Version getLastVersion()
	{
		return version;
	}

	public boolean exclusiveLock()
	{
		return rwLock.writeLock().tryLock();
	}

	public void exclusiveUnlock()
	{
		rwLock.writeLock().unlock();
	}

	public boolean sharedLock()
	{
		return rwLock.readLock().tryLock();
	}

	public void sharedUnlock()
	{
		rwLock.readLock().unlock();
	}
}
