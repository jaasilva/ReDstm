package org.deuce.benchmark.intset;

import org.deuce.distribution.replication.partial.Partial;

public class _Node
{
	int k; // key
	@Partial
	Object v; // val
	_Node p; // parent
	_Node l; // left
	_Node r; // right
	int c; // color

	public _Node()
	{
	}
}
