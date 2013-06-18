package test.partial;

public class PartialList<T>
{
	protected _PartialNode<T> head;

	public PartialList()
	{
		head = new _PartialNode<T>(0, null, null);
	}

	public void add(int key, T val)
	{
		this.head = head.cons(key, val);
	}
}
