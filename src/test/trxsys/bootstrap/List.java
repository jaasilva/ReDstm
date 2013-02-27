package trxsys.bootstrap;

public class List
{
	public Node head = new Node();

	public String asString()
	{
		return toString() + "->" + (head != null ? head.asString() : "null");
	}
}
