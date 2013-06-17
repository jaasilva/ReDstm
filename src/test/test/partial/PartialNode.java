package test.partial;

import org.deuce.distribution.replication.partial.Partial;

public class PartialNode<T> extends Node<T>
{
	@Partial
	private T value;

	public PartialNode(T val, Node<T> n)
	{
		super(val, n);
	}

	@Override
	public T getValue()
	{
		return this.value;
	}

	@Override
	public void setValue(T val)
	{
		this.value = val;
	}

	@Override
	public Node<T> cons(T val)
	{
		return new PartialNode<T>(val, this);
	}
}
