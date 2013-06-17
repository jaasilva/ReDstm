package test.partial;

import org.deuce.distribution.replication.partial.Partial;

public class _PartialNode<T>
{
	private _PartialNode<T> next;
	@Partial
	private T value;

	public _PartialNode(T val, _PartialNode<T> n)
	{
		this.next = n;
		this.value = val;
	}

	public _PartialNode<T> getNext()
	{
		return this.next;
	}

	public void setNext(_PartialNode<T> n)
	{
		this.next = n;
	}

	public T getValue()
	{
		return this.value;
	}

	public void setValue(T val)
	{
		this.value = val;
	}

	public _PartialNode<T> cons(T val)
	{
		return new _PartialNode<T>(val, this);
	}
}
