package org.deuce.benchmark.intset;

/**
 * @author Pascal Felber
 * @since 0.1
 */
public interface IntSet
{
	public boolean add(int value);

	public boolean remove(int value);

	public boolean contains(int value);

	public boolean validate();

	public boolean initAdd(int value);
}
