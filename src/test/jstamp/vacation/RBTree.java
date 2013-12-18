package jstamp.vacation;

import org.deuce.distribution.replication.partial.Partial;

/*
 * =============================================================================
 * rbtree.java -- Red-black balanced binary search tree
 * =============================================================================
 * Copyright (C) Sun Microsystems Inc., 2006. All Rights Reserved. Authors: Dave
 * Dice, Nir Shavit, Ori Shalev. STM: Transactional Locking for Disjoint Access
 * Parallelism Transactional Locking II, Dave Dice, Ori Shalev, Nir Shavit DISC
 * 2006, Sept 2006, Stockholm, Sweden.
 * =============================================================================
 * Modified by Chi Cao Minh
 * =============================================================================
 * For the license of bayes/sort.h and bayes/sort.c, please see the header of
 * the files.
 * ----------------------------------------------------------------------------
 * For the license of kmeans, please see kmeans/LICENSE.kmeans
 * ----------------------------------------------------------------------------
 * For the license of ssca2, please see ssca2/COPYRIGHT
 * ----------------------------------------------------------------------------
 * For the license of lib/mt19937ar.c and lib/mt19937ar.h, please see the header
 * of the files.
 * ----------------------------------------------------------------------------
 * For the license of lib/rbtree.h and lib/rbtree.c, please see
 * lib/LEGALNOTICE.rbtree and lib/LICENSE.rbtree
 * ----------------------------------------------------------------------------
 * Unless otherwise noted, the following license applies to STAMP files:
 * Copyright (c) 2007, Stanford University All rights reserved. Redistribution
 * and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met: * Redistributions
 * of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer. * Redistributions in binary form
 * must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided
 * with the distribution. * Neither the name of Stanford University nor the
 * names of its contributors may be used to endorse or promote products derived
 * from this software without specific prior written permission. THIS SOFTWARE
 * IS PROVIDED BY STANFORD UNIVERSITY ``AS IS'' AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO
 * EVENT SHALL STANFORD UNIVERSITY BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * =============================================================================
 */

public class RBTree
{
	public class Node
	{
		int k; // key
		@Partial
		Object v; // val
		Node p; // parent
		Node l; // left
		Node r; // right
		int c; // color

		public Node()
		{
		}

		@Override
		public Node clone()
		{ // Replaces 'this' with a fresh node
			Node x = new Node();
			x.k = this.k;
			x.v = this.v;
			x.p = this.p;
			x.l = this.l;
			x.r = this.r;
			x.c = this.c;

			if (x.p != null)
			{ // replace parent pointer
				if (x.p.l == this)
				{ // 'this' is p's left child
					x.p.l = x;
				}
				else
				{ // 'this' is p's right child
					x.p.r = x;
				}
			}
			else
			{ // if 'this' is the root I have to update the reference
				root = x;
			}

			// replace children pointers
			if (x.l != null)
			{
				x.l.p = x;
			}
			if (x.r != null)
			{
				x.r.p = x;
			}

			return x;
		}
	}

	private Node root;

	public RBTree()
	{
	}

	/*****************************************
	 * private methods
	 *****************************************/

	private Node lookup(int k)
	{
		Node p = root;

		while (p != null)
		{
			int cmp = compare(k, p.k);
			if (cmp == 0)
			{
				return p;
			}
			p = (cmp < 0) ? p.l : p.r;
		}

		return null;
	}

	private void rotateLeft(Node x)
	{
		Node r = x.r;
		Node rl = r.l;
		x.r = rl;
		if (rl != null)
		{
			rl.p = x;
		}

		Node xp = x.p;
		r.p = xp;
		if (xp == null)
		{
			root = r;
		}
		else if (xp.l == x)
		{
			xp.l = r;
		}
		else
		{
			xp.r = r;
		}
		r.l = x;
		x.p = r;
	}

	private void rotateRight(Node x)
	{
		Node l = x.l;
		Node lr = l.r;
		x.l = lr;
		if (lr != null)
		{
			lr.p = x;
		}
		Node xp = x.p;
		l.p = xp;
		if (xp == null)
		{
			root = l;
		}
		else if (xp.r == x)
		{
			xp.r = l;
		}
		else
		{
			xp.l = l;
		}

		l.r = x;
		x.p = l;
	}

	private Node parentOf(Node n)
	{
		return ((n != null) ? n.p : null);
	}

	private Node leftOf(Node n)
	{
		return ((n != null) ? n.l : null);
	}

	private Node rightOf(Node n)
	{
		return ((n != null) ? n.r : null);
	}

	private int colorOf(Node n)
	{
		return ((n != null) ? n.c : Defines.BLACK);
	}

	private void setColor(Node n, int c)
	{
		if (n != null)
		{
			n.c = c;
		}
	}

	private void fixAfterInsertion(Node x)
	{
		x.c = Defines.RED;

		while (x != null && x != root)
		{
			Node xp = x.p;
			if (xp.c != Defines.RED)
			{
				break;
			}

			if (parentOf(x) == leftOf(parentOf(parentOf(x))))
			{
				Node y = rightOf(parentOf(parentOf(x)));
				if (colorOf(y) == Defines.RED)
				{
					setColor(parentOf(x), Defines.BLACK);
					setColor(y, Defines.BLACK);
					setColor(parentOf(parentOf(x)), Defines.RED);
					x = parentOf(parentOf(x));
				}
				else
				{
					if (x == rightOf(parentOf(x)))
					{
						x = parentOf(x);
						rotateLeft(x);
					}
					setColor(parentOf(x), Defines.BLACK);
					setColor(parentOf(parentOf(x)), Defines.RED);
					if (parentOf(parentOf(x)) != null)
					{
						rotateRight(parentOf(parentOf(x)));
					}
				}
			}
			else
			{
				Node y = leftOf(parentOf(parentOf(x)));
				if (colorOf(y) == Defines.RED)
				{
					setColor(parentOf(x), Defines.BLACK);
					setColor(y, Defines.BLACK);
					setColor(parentOf(parentOf(x)), Defines.RED);
					x = parentOf(parentOf(x));
				}
				else
				{
					if (x == leftOf(parentOf(x)))
					{
						x = parentOf(x);
						rotateRight(x);
					}
					setColor(parentOf(x), Defines.BLACK);
					setColor(parentOf(parentOf(x)), Defines.RED);
					if (parentOf(parentOf(x)) != null)
					{
						rotateLeft(parentOf(parentOf(x)));
					}
				}
			}
		}

		Node ro = root;
		if (ro.c != Defines.BLACK)
		{
			ro.c = Defines.BLACK;
		}
	}

	private Node insert(int k, Object v, Node n)
	{
		Node t = root;
		if (t == null)
		{
			if (n == null)
			{
				return null;
			}
			/* Note: the following STs don't really need to be transactional */
			n.l = null;
			n.r = null;
			n.p = null;
			n.k = k;
			n.v = v;
			n.c = Defines.BLACK;
			root = n;
			return null;
		}

		while (true)
		{
			int cmp = compare(k, t.k);
			if (cmp == 0)
			{
				return t;
			}
			else if (cmp < 0)
			{
				Node tl = t.l;
				if (tl != null)
				{
					t = tl;
				}
				else
				{
					n.l = null;
					n.r = null;
					n.k = k;
					n.v = v;
					n.p = t;
					t.l = n;
					fixAfterInsertion(n);
					return null;
				}
			}
			else
			{ /* cmp > 0 */
				Node tr = t.r;
				if (tr != null)
				{
					t = tr;
				}
				else
				{
					n.l = null;
					n.r = null;
					n.k = k;
					n.v = v;
					n.p = t;
					t.r = n;
					fixAfterInsertion(n);
					return null;
				}
			}
		}
	}

	private Node successor(Node t)
	{
		if (t == null)
		{
			return null;
		}
		else if (t.r != null)
		{
			Node p = t.r;
			while (p.l != null)
			{
				p = p.l;
			}
			return p;
		}
		else
		{
			Node p = t.p;
			Node ch = t;
			while (p != null && ch == p.r)
			{
				ch = p;
				p = p.p;
			}
			return p;
		}

	}

	private void fixAfterDeletion(Node x)
	{
		while (x != root && colorOf(x) == Defines.BLACK)
		{
			if (x == leftOf(parentOf(x)))
			{
				Node sib = rightOf(parentOf(x));
				if (colorOf(sib) == Defines.RED)
				{
					setColor(sib, Defines.BLACK);
					setColor(parentOf(x), Defines.RED);
					rotateLeft(parentOf(x));
					sib = rightOf(parentOf(x));
				}
				if (colorOf(leftOf(sib)) == Defines.BLACK
						&& colorOf(rightOf(sib)) == Defines.BLACK)
				{
					setColor(sib, Defines.RED);
					x = parentOf(x);
				}
				else
				{
					if (colorOf(rightOf(sib)) == Defines.BLACK)
					{
						setColor(leftOf(sib), Defines.BLACK);
						setColor(sib, Defines.RED);
						rotateRight(sib);
						sib = rightOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), Defines.BLACK);
					setColor(rightOf(sib), Defines.BLACK);
					rotateLeft(parentOf(x));
					x = root;
				}
			}
			else
			{ /* symmetric */
				Node sib = leftOf(parentOf(x));
				if (colorOf(sib) == Defines.RED)
				{
					setColor(sib, Defines.BLACK);
					setColor(parentOf(x), Defines.RED);
					rotateRight(parentOf(x));
					sib = leftOf(parentOf(x));
				}
				if (colorOf(rightOf(sib)) == Defines.BLACK
						&& colorOf(leftOf(sib)) == Defines.BLACK)
				{
					setColor(sib, Defines.RED);
					x = parentOf(x);
				}
				else
				{
					if (colorOf(leftOf(sib)) == Defines.BLACK)
					{
						setColor(rightOf(sib), Defines.BLACK);
						setColor(sib, Defines.RED);
						rotateLeft(sib);
						sib = leftOf(parentOf(x));
					}
					setColor(sib, colorOf(parentOf(x)));
					setColor(parentOf(x), Defines.BLACK);
					setColor(leftOf(sib), Defines.BLACK);
					rotateRight(parentOf(x));
					x = root;
				}
			}
		}

		if (x != null && x.c != Defines.BLACK)
		{
			x.c = Defines.BLACK;
		}
	}

	private Node deleteNode(Node p)
	{
		/*
		 * If strictly internal, copy successor's element to p and then make p
		 * point to successor
		 */
		if (p.l != null && p.r != null)
		{
			Node s = successor(p);

			Node x = p.clone();
			x.k = s.k;
			x.v = s.v;

			// p.k = s.k;
			// p.v = s.v;
			p = s;
		} /* p has 2 children */

		/* Start fixup at replacement node, if it exists */
		Node replacement = (p.l != null) ? p.l : p.r;

		if (replacement != null)
		{
			/* Link replacement to parent */
			replacement.p = p.p;
			Node pp = p.p;
			if (pp == null)
			{
				root = replacement;
			}
			else if (p == pp.l)
			{
				pp.l = replacement;
			}
			else
			{
				pp.r = replacement;
			}

			/* Null out links so they are OK to use by fixAfterDeletion */
			p.l = null;
			p.r = null;
			p.p = null;

			/* Fix replacement */
			if (p.c == Defines.BLACK)
			{
				fixAfterDeletion(replacement);
			}
		}
		else if (p.p == null)
		{ /* return if we are the only node */
			root = null;
		}
		else
		{ /* No children. Use self as phantom replacement and unlink */
			if (p.c == Defines.BLACK)
			{
				fixAfterDeletion(p);
			}
			Node pp = p.p;
			if (pp != null)
			{
				if (p == pp.l)
				{
					pp.l = null;
				}
				else if (p == pp.r)
				{
					pp.r = null;
				}
				p.p = null;
			}
		}
		return p;
	}

	/*****************************************
	 * diagnostic section
	 *****************************************/

	private Node firstEntry()
	{
		Node p = root;
		if (p != null)
		{
			while (p.l != null)
			{
				p = p.l;
			}
		}
		return p;
	}

	private int verifyRedBlack(Node root, int depth)
	{
		int height_left;
		int height_right;

		if (root == null)
		{
			return 1;
		}

		height_left = verifyRedBlack(root.l, depth + 1);
		height_right = verifyRedBlack(root.r, depth + 1);
		if (height_left == 0 || height_right == 0)
		{
			return 0;
		}
		if (height_left != height_right)
		{
			System.out.println(" Imbalace @depth = " + depth + " : "
					+ height_left + " " + height_right);
		}

		if (root.l != null && root.l.p != root)
		{
			System.out.println(" lineage");
		}
		if (root.r != null && root.r.p != root)
		{
			System.out.println(" lineage");
		}

		/* Red-Black alternation */
		if (root.c == Defines.RED)
		{
			if (root.l != null && root.l.c != Defines.BLACK)
			{
				System.out.println("VERIFY in verifyRedBlack");
				return 0;
			}

			if (root.r != null && root.r.c != Defines.BLACK)
			{
				System.out.println("VERIFY in verifyRedBlack");
				return 0;
			}
			return height_left;
		}
		if (root.c != Defines.BLACK)
		{
			System.out.println("VERIFY in verifyRedBlack");
			return 0;
		}

		return (height_left + 1);
	}

	private int compare(int a, int b)
	{
		return a - b;
	}

	public void print()
	{
		printHelper(root, 0);
	}

	private static final int INDENT_STEP = 4;

	private static void printHelper(Node n, int indent)
	{
		if (n == null)
		{
			System.out.print("<->");
			return;
		}
		if (n.r != null)
		{
			printHelper(n.r, indent + INDENT_STEP);
		}
		for (int i = 0; i < indent; i++)
			System.out.print(" ");
		if (n.c == Defines.BLACK)
			System.out.println(n.k);
		else
			System.out.println("<" + n.k + ">");
		if (n.l != null)
		{
			printHelper(n.l, indent + INDENT_STEP);
		}
	}

	public void printBinaryTree(Node root, int level)
	{
		if (root == null)
			return;
		printBinaryTree(root.r, level + 1);
		if (level != 0)
		{
			for (int i = 0; i < level - 1; i++)
				System.out.print("|\t");
			if (root.c == Defines.BLACK)
				System.out.println("|-------@" + root.k);
			else
				System.out.println("|-------O" + root.k);
		}
		else
		{
			if (root.c == Defines.BLACK)
				System.out.println("@" + root.k);
			else
				System.out.println("O" + root.k);
		}
		printBinaryTree(root.l, level + 1);
	}

	/*****************************************
	 * public methods
	 *****************************************/

	public int verify(int verbose)
	{
		if (root == null)
		{
			return 1;
		}
		if (verbose != 0)
		{
			System.out.println("Integrity check: ");
		}

		if (root.p != null)
		{
			System.out.println("  (WARNING) root = " + root + " parent = "
					+ root.p);
			return -1;
		}
		if (root.c != Defines.BLACK)
		{
			System.out.println("  (WARNING) root = " + root + " color = "
					+ root.c);
		}

		/* Weak check of binary-tree property */
		int ctr = 0;
		Node its = firstEntry();
		while (its != null)
		{
			ctr++;
			Node child = its.l;
			if (child != null && child.p != its)
			{
				System.out.println("bad parent");
			}
			child = its.r;
			if (child != null && child.p != its)
			{
				System.out.println("Bad parent");
			}
			Node nxt = successor(its);
			if (nxt == null)
			{
				break;
			}
			if (compare(its.k, nxt.k) >= 0)
			{
				System.out.println("Key order " + its + " (" + its.k + " "
						+ its.v + ") " + nxt + " (" + nxt.k + " " + nxt.v
						+ ") ");
				return -3;
			}
			its = nxt;
		}

		int vfy = verifyRedBlack(root, 0);
		if (verbose != 0)
		{
			System.out.println(" Nodes = " + ctr + " Depth = " + vfy);
		}

		return vfy;
	}

	public boolean insert(int key, Object val)
	{
		Node node = new Node();
		Node ex = insert(key, val, node);
		if (ex != null)
		{
			node = null;
		}
		return ex == null;
	}

	public boolean remove(int key)
	{
		Node node = null;
		node = lookup(key);

		if (node != null)
		{
			node = deleteNode(node);
		}
		return node != null;
	}

	public boolean update(int key, Object val)
	{
		Node nn = new Node();
		Node ex = insert(key, val, nn);
		if (ex != null)
		{
			ex.v = val;
			nn = null;
			return true;
		}
		return false;
	}

	public Object find(int key)
	{
		Node n = lookup(key);
		if (n != null)
		{
			Object val = n.v;
			return val;
		}
		return null;
	}

	public boolean contains(int key)
	{
		Node n = lookup(key);

		return (n != null);
	}
}
