package org.deuce.benchmark.partial.photos;

import org.deuce.distribution.replication.partial.Partial;

public class RBTreeNode
{
	int k; // key
	@Partial
	Object v; // val
	RBTreeNode p; // parent
	RBTreeNode l; // left
	RBTreeNode r; // right
	int c; // color

	public RBTreeNode()
	{
	}
}
