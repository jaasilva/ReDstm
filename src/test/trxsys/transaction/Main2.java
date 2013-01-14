package trxsys.transaction;

import java.util.Scanner;

import org.deuce.Atomic;
import org.deuce.distribution.replication.full.Bootstrap;


public class Main2 {
	@Bootstrap(id = 1)
	static List list;

	public static void main(String[] args) throws InterruptedException {
//		createList();
		
		barrier();
		
		printList();
		
		list.insert(new Node(2));

		list.remove();
		
		barrier();
		
		printList();
	}

	private static void barrier() {
		System.out.println("Press enter...");
		new Scanner(System.in).nextLine();
	}

	@Atomic
	public static void createList() {
		list = new List();
	}
	
	@Atomic
	public static void printList() {
		System.out.println(list.asString());
	}
}
