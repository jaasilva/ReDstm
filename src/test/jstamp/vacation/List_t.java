package jstamp.vacation;

/*
 * =============================================================================
 * List_t.java -- Sorted singly linked list -- Options: duplicate allowed
 * (DLIST_NO_DUPLICATES) is no implemented yet (default: allow duplicates)
 * =============================================================================
 * Copyright (C) Stanford University, 2006. All Rights Reserved. Author: Chi Cao
 * Minh
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

public class List_t
{
	protected List_Node head;
	private int size;

	public List_t()
	{
		head = new List_Node();
	}

	/**
	 * Returns null on failure
	 */
	private List_Node allocNode(Object dataPtr)
	{
		List_Node nodePtr = new List_Node();

		nodePtr.dataPtr = dataPtr;
		nodePtr.nextPtr = null;

		return nodePtr;
	}

	public static List_t alloc()
	{
		List_t listPtr = new List_t();

		listPtr.head.dataPtr = null;
		listPtr.head.nextPtr = null;
		listPtr.size = 0;

		return listPtr;
	}

	public static void free(List_t listPtr)
	{
		listPtr = null;
	}

	/**
	 * Return TRUE if list is empty, else FALSE
	 */
	public boolean isEmpty()
	{
		return (head.nextPtr == null);
	}

	/**
	 * Returns size of list
	 */
	public int getSize()
	{
		return size;
	}

	private List_Node findPrevious(Object dataPtr)
	{
		List_Node prevPtr = head;
		List_Node nodePtr = prevPtr.nextPtr;

		for (; nodePtr != null; nodePtr = nodePtr.nextPtr)
		{
			if (compare(nodePtr.dataPtr, dataPtr) >= 0)
			{
				return prevPtr;
			}
			prevPtr = nodePtr;
		}

		return prevPtr;
	}

	/**
	 * Returns NULL if not found, else returns pointer to data
	 */
	public Object find(Object dataPtr)
	{
		List_Node nodePtr;
		List_Node prevPtr = findPrevious(dataPtr);

		nodePtr = prevPtr.nextPtr;

		if ((nodePtr == null) || (compare(nodePtr.dataPtr, dataPtr) != 0))
		{
			return null;
		}

		return (nodePtr.dataPtr);
	}

	public int compare(Object obj1, Object obj2)
	{
		Reservation_Info aPtr = (Reservation_Info) obj1;
		Reservation_Info bPtr = (Reservation_Info) obj2;
		int typeDiff;

		typeDiff = aPtr.type - bPtr.type;

		return ((typeDiff != 0) ? (typeDiff) : (aPtr.id - bPtr.id));
	}

	/**
	 * Return TRUE on success, else FALSE
	 */
	public boolean insert(Object dataPtr)
	{
		List_Node prevPtr;
		List_Node nodePtr;
		List_Node currPtr;

		prevPtr = findPrevious(dataPtr);
		currPtr = prevPtr.nextPtr;

		nodePtr = allocNode(dataPtr);

		nodePtr.nextPtr = currPtr;
		prevPtr.nextPtr = nodePtr;
		size++;

		return true;
	}

	/**
	 * Returns TRUE if successful, else FALSE
	 */
	public boolean remove(Object dataPtr)
	{
		List_Node prevPtr;
		List_Node nodePtr;

		prevPtr = findPrevious(dataPtr);

		nodePtr = prevPtr.nextPtr;

		if ((nodePtr != null) && (compare(nodePtr.dataPtr, dataPtr) == 0))
		{
			prevPtr.nextPtr = nodePtr.nextPtr;
			nodePtr.nextPtr = null;
			nodePtr = null;
			size--;

			return true;
		}

		return false;
	}

	public int compareObject(Object obj1, Object obj2)
	{
		return 1;
	}

	/**
	 * Removes all elements
	 */
	public void clear()
	{
		head = new List_Node();
		size = 0;
	}
}
