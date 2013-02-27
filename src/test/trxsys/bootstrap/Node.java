package trxsys.bootstrap;

public class Node
{
	int value;
	Node next;

	public String asString()
	{
		return toString() + "->" + (next != null ? next.asString() : "null");
	}
}
