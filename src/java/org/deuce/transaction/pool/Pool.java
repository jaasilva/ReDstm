package org.deuce.transaction.pool;

import org.deuce.transform.ExcludeTM;

/**
 * Represents the transaction read set. And acts as a recycle pool of the
 * {@link ReadFieldAccess}.
 * 
 * @author Guy Korland
 * @since 1.0
 */
@ExcludeTM
final public class Pool<T>
{

	private static final int DEFAULT_CAPACITY = 1024;
	@SuppressWarnings("unchecked")
	private T[] pool = (T[]) new Object[DEFAULT_CAPACITY];
	private int nextAvaliable = 0;
	final private ResourceFactory<T> factory;

	public Pool(ResourceFactory<T> factory)
	{
		this.factory = factory;
		fillArray(0);
	}

	public void clear()
	{
		nextAvaliable = 0;
	}

	private void fillArray(int offset)
	{
		for (int i = offset; i < DEFAULT_CAPACITY + offset; ++i)
		{
			pool[i] = factory.newInstance();
		}
	}

	public T getNext()
	{
		if (nextAvaliable >= pool.length)
		{
			int orignLength = pool.length;
			@SuppressWarnings("unchecked")
			T[] newPool = (T[]) new Object[orignLength + DEFAULT_CAPACITY];
			System.arraycopy(pool, 0, newPool, 0, pool.length);
			pool = newPool;
			fillArray(orignLength);
		}
		return pool[nextAvaliable++];
	}
}
