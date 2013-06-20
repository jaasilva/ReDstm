package jstamp.vacation;

import org.deuce.distribution.replication.partial.Partial;

public class Node
{
	int k; // key
	@Partial Object v; // val
	Node p; // parent
	Node l; // left
	Node r; // right
	int c; // color

	public Node()
	{
	}

}
