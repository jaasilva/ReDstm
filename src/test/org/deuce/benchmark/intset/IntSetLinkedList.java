package org.deuce.benchmark.intset;

import org.deuce.*;
import org.deuce.distribution.replication.partial.Partial;

/**
 * @author Pascal Felber
 * @since 0.1
 */
public class IntSetLinkedList implements IntSet
{
	public class Node
	{
		final private int m_key;
		@Partial
		Object m_value;
		Node m_next;

		public Node(int value, Node next)
		{
			m_key = value;
			m_value = value;
			m_next = next;
		}

		public Node(int key)
		{
			this(key, null);
		}

		public int getKey()
		{
			return m_key;
		}

		public Object getValue()
		{
			return m_value;
		}

		public void setNext(Node next)
		{
			m_next = next;
		}

		public Node getNext()
		{
			return m_next;
		}

		public String toString()
		{
			return Integer.toString(m_key);
		}
	}

	final private Node m_head;

	public IntSetLinkedList()
	{
		m_head = new Node(Integer.MIN_VALUE);
		init();
	}

	@Atomic
	private void init()
	{
		Node min = m_head;
		Node max = new Node(Integer.MAX_VALUE);
		min.setNext(max);
	}

	@Atomic
	public boolean add(int key)
	{
		boolean result;

		Node previous = m_head;
		Node next = previous.getNext();
		int v;
		while ((v = next.getKey()) < key)
		{
			previous = next;
			next = previous.getNext();
		}
		result = v != key;
		if (result)
		{
			previous.setNext(new Node(key, next));
		}

		return result;
	}

	@Atomic
	public boolean remove(int key)
	{
		boolean result;

		Node previous = m_head;
		Node next = previous.getNext();
		int v;
		while ((v = next.getKey()) < key)
		{
			previous = next;
			next = previous.getNext();
		}
		result = v == key;
		if (result)
		{
			previous.setNext(next.getNext());
		}

		return result;
	}

	@Atomic
	public boolean contains(int key)
	{
		boolean result;

		Node previous = m_head;
		Node next = previous.getNext();
		int v;
		while ((v = next.getKey()) < key)
		{
			previous = next;
			next = previous.getNext();
		}
		result = (v == key);
		if (result)
		{
			Object x = next.m_value;
		}

		return result;
	}

	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		Node n = m_head;
		while (n != null)
		{
			sb.append(n);
			n = n.getNext();
			if (n != null)
				sb.append(' ');
		}
		return sb.toString();
	}

	@Override
	public boolean validate()
	{
		// TODO Auto-generated method stub
		return false;
	}
}
