package test;

import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

public class mainTest
{

	public static void main(String[] args)
	{
		
		// BlockingQueue<Pair<Integer, String>> waitingPrepare = new
		// PriorityBlockingQueue<Pair<Integer, String>>(
		// 50, comp2);

		Queue<Pair> waitingPrepare = new PriorityQueue<Pair>(50);

		mainTest x = new mainTest();

		Pair a = x.new Pair("LOL1",
				Integer.parseInt("ubuntu-10907".split("-")[1]) );
		Pair b = x.new Pair("LOL2",
				Integer.parseInt("ubuntu-9039".split("-")[1]));
		Pair c = x.new Pair("LOL3",
				Integer.parseInt("ubuntu-65296".split("-")[1]));
		Pair d = x.new Pair("LOL4",
				Integer.parseInt("ubuntu-32365".split("-")[1]));
		Pair e = x.new Pair("LOL5",
				Integer.parseInt("ubuntu-43233".split("-")[1]));

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
		
//		System.out.println("> "+Thread.currentThread().getId());
//		
//		for (int i = 0; i < 5; i++)
//		{
//			X z = x.new X();
//			z.start();
//		}
	}
	
	class X extends Thread
	{
		@Override
		public void run()
		{
			System.out.println("- "+Thread.currentThread().getId());
		}
	}

	class Pair implements Comparable<Pair>
	{
		public String first;
		public int second;

		public Pair(String first, int second)
		{
			this.first = first;
			this.second = second;
		}

		@Override
		public boolean equals(Object other)
		{
			return (other instanceof Pair)
					&& (this.first.equals(((Pair) other).first));
		}

		@Override
		public String toString()
		{
			return "(" + first + "," + second + ")";
		}

		@Override
		public int compareTo(Pair other)
		{
			return this.second - other.second;
		}
	}
}
