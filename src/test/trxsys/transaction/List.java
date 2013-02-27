package trxsys.transaction;

import org.deuce.Atomic;

public class List
{
	public Node head;

	@Atomic
	public void insert(Node node)
	{
		node.next = head;
		head = node;
	}

	@Atomic
	public void remove()
	{
		head = head.next;
	}

	@Atomic
	public String asString()
	{
		return toString() + "->" + (head != null ? head.asString() : "null");
	}
}
