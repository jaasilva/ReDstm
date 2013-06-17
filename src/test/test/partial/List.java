package test.partial;

import org.deuce.Atomic;

public class List<T>
{
	public static final int PARTIAL = 1;
	public static final int TOTAL = 2;

	protected Node<T> head;

	public List(int mode)
	{
		switch (mode)
		{
			case PARTIAL:
				head = new PartialNode<T>(null, null);
				break;
			case TOTAL:
				head = new TotalNode<T>(null, null);
				break;
		}
	}

	public List()
	{
		this(TOTAL);
	}

	@Atomic
	public void add(T val)
	{
		this.head = head.cons(val);
	}
}
