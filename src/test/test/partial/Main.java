package test.partial;

import org.deuce.Atomic;
import org.deuce.benchmark.Barrier;
import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.replication.Bootstrap;
import org.deuce.distribution.replication.partial.oid.PartialReplicationOID;

class MyObject {
	String string;

	public MyObject(final String string) {
		this.string = string;
	}
}
public class Main
{
	@Bootstrap(id = 1)
	static PartialList<MyObject> intset;

	@Bootstrap(id = 2)
	static public Barrier start;

	@Bootstrap(id = 3)
	static public Barrier end;

	@Bootstrap(id = 4)
	static public Barrier close;

	public static void main(String[] args)
	{
		System.out.println("...");

		if (Integer.getInteger("tribu.site") == 1)
		{
			initSet();
			System.out.println("-- List initialized.");
		}

		int replicas = Integer.getInteger("tribu.replicas");
		initBarriers(replicas);
		System.out.println("-- Barriers initialized.");

		System.out.println("-- Starting...");
		start.join();

		add(Integer.getInteger("tribu.site") * 3, new MyObject("two"));
		add(Integer.getInteger("tribu.site") * 3, new MyObject("one"));

		System.out.println("-- Ending...");
		end.join();

		printList(intset);
		System.out.println("---");
		printLocalList(intset);

		System.out.println("-- Closing...");
		close.join();

		TribuDSTM.close();
	}

	@Atomic
	private static void initSet()
	{
		if (intset == null)
		{
			intset = new PartialList<MyObject>();
		}
	}

	@Atomic
	private static void initBarriers(int replicas)
	{
		if (start == null)
			start = new Barrier(replicas);
		if (end == null)
			end = new Barrier(replicas);
		if (close == null)
			close = new Barrier(replicas);
	}

	@Atomic
	private static void add(int key, MyObject val)
	{
		intset.add(key, val);
	}

	@Atomic
	private static void printList(PartialList<MyObject> list)
	{
		_PartialNode<MyObject> n = list.head;
		while (n != null) {
			MyObject value = n.getValue();
			if (value == null) {
				System.out.println(n.key+": null");
			} else {
				ObjectMetadata m = ((UniqueObject) value).getMetadata();
				PartialReplicationOID pm = (PartialReplicationOID) m;
				System.out.println(n.key + ": " + value.string + " ("
						+ pm.getGroup() + ")");
			}
			n = n.getNext();
		}
	}

	private static void printLocalList(PartialList<MyObject> list) {
		_PartialNode<MyObject> n = list.head;
		while (n != null) {
			MyObject value = n.getValue();
			System.out.print(n.key + ": ");
			System.out.println(value == null ? "null" : value.string);
			n = n.getNext();
		}
	}
}
