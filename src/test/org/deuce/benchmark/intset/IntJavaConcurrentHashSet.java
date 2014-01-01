package org.deuce.benchmark.intset;

import java.util.concurrent.ConcurrentHashMap;

public class IntJavaConcurrentHashSet implements IntSet
{
	// Dummy value to associate with an Object in the backing Map
	private static final Object PRESENT = new Object();
	private final ConcurrentHashMap<Integer, Object> map = new ConcurrentHashMap<Integer, Object>();

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

	@Override
	public boolean initAdd(int value)
	{
		return add(value);
	}
}
