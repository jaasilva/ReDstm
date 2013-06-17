package test.partial;

public class TotalNode<T> extends Node<T>
{
	protected T value;

	public TotalNode(T val, Node<T> n)
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
		return new TotalNode<T>(val, this);
	}
}
