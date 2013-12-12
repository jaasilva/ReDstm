package org.deuce.benchmark.intset;

import java.util.Hashtable;

public class HashTableSet implements IntSet
{
	// Dummy value to associate with an Object in the backing Map
	private static final Object PRESENT = new Object();
	private final Hashtable<Integer, Object> map = new Hashtable<Integer, Object>();

	@Override
	public boolean add(int value)
	{
		return map.put(value, PRESENT) == null;
	}

	@Override
	public boolean contains(int value)
	{
		return map.containsKey(value);
	}

	@Override
	public boolean remove(int value)
	{
		return map.remove(value) != null;
	}

	@Override
	public boolean validate()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
