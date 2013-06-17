package test.partial;

public class PartialList<T>
{
	protected _PartialNode<T> head;

	public PartialList()
	{
	}

	public void init()
	{
		head = new _PartialNode<T>(null, null);
	}

	public void add(T val)
	{
		this.head = head.cons(val);
	}
}
