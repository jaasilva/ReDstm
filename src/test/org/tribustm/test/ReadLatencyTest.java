package org.tribustm.test;

import org.deuce.Atomic;
import org.deuce.benchmark.intset.IntSetLinkedList;


public class ReadLatencyTest {
	
	public static final int MAX = 1000;
	
	public static IntSetLinkedList list = new IntSetLinkedList();
	static {
		for (int i=0; i < MAX; i++) {
			list.add(i);
		}
	}
	
	@Atomic
	public static void doReads() {
		for (int i=0; i < MAX; i++) {
			list.contains(i);
		}
	}
	
	public static void main(String[] args) {
		doReads();
	}

}
