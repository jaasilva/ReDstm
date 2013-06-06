package test;

import java.util.Comparator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class mainTest
{

	public static void main(String[] args)
	{
		Comparator<Pair<Integer, String>> comp2 = new Comparator<Pair<Integer, String>>()
		{
			@Override
			public int compare(Pair<Integer, String> o1,
					Pair<Integer, String> o2)
			{
				return o2.first - o1.first;
			}
		};
		// BlockingQueue<Pair<Integer, String>> waitingPrepare = new
		// PriorityBlockingQueue<Pair<Integer, String>>(
		// 50, comp2);

		BlockingQueue<Pair<Integer, String>> waitingPrepare = new LinkedBlockingQueue<Pair<Integer, String>>(
				50);

		mainTest x = new mainTest();

		Pair<Integer, String> a = x.new Pair<Integer, String>(
				Integer.parseInt("ubuntu-10907".split("-")[1]), "LOL1");
		Pair<Integer, String> b = x.new Pair<Integer, String>(
				Integer.parseInt("ubuntu-9039".split("-")[1]), "LOL2");
		Pair<Integer, String> c = x.new Pair<Integer, String>(
				Integer.parseInt("ubuntu-65296".split("-")[1]), "LOL3");
		Pair<Integer, String> d = x.new Pair<Integer, String>(
				Integer.parseInt("ubuntu-32365".split("-")[1]), "LOL4");
		Pair<Integer, String> e = x.new Pair<Integer, String>(
				Integer.parseInt("ubuntu-43233".split("-")[1]), "LOL5");

		waitingPrepare.add(b);
		waitingPrepare.add(a);
		waitingPrepare.add(e);
		waitingPrepare.add(c);
		waitingPrepare.add(d);

		System.out.println(waitingPrepare.poll());
		System.out.println(waitingPrepare.poll());
		System.out.println(waitingPrepare.poll());
		System.out.println(waitingPrepare.poll());
		System.out.println(waitingPrepare.poll());

	}

	class Pair<K, V>
	{
		public K first;
		public V second;

		public Pair(K first, V second)
		{
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean equals(Object other)
		{
			return (other instanceof Pair)
					&& (this.first.equals(((Pair<?, ?>) other).first));
		}

		public String toString()
		{
			return "(" + first + "," + second + ")";
		}
	}
}
