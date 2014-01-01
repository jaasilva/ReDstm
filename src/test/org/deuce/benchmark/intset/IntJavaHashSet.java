package org.deuce.benchmark.intset;

import org.deuce.benchmark.java.util.HashSet;

public class IntJavaHashSet implements IntSet
{
	private final HashSet<Integer> set = new HashSet<Integer>();

	@Override
	public boolean add(int value)
	{
		return set.add(value);
	}

	@Override
	public boolean contains(int value)
	{
		return set.contains(value);
	}

	@Override
	public boolean remove(int value)
	{
		return set.remove(value);
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
