package test.partial;

public abstract class Node<T>
{
	protected Node<T> next;

	public Node(T val, Node<T> n)
	{
		this.next = n;
		this.setValue(val);
	}

	public abstract T getValue();

	public abstract void setValue(T val);

	public Node<T> getNext()
	{
		return this.next;
	}

	public void setNext(Node<T> n)
	{
		this.next = n;
	}

	public abstract Node<T> cons(T val);
}
