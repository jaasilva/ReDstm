package jstamp.genome;

import org.deuce.Atomic;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.Bootstrap;
import org.deuce.profiling.Profiler;
import org.deuce.transaction.TransactionException;

public class Genome extends Thread
{
	@Bootstrap(id = 1)
	static org.deuce.benchmark.Barrier setupBarrier;
	@Bootstrap(id = 2)
	static org.deuce.benchmark.Barrier finishBarrier;
	@Bootstrap(id = 3)
	static org.deuce.benchmark.Barrier benchBarrier1;
	@Bootstrap(id = 4)
	static org.deuce.benchmark.Barrier benchBarrier2;
	@Bootstrap(id = 5)
	static org.deuce.benchmark.Barrier benchBarrier3;
	@Bootstrap(id = 6)
	static org.deuce.benchmark.Barrier benchBarrier4;
	@Bootstrap(id = 7)
	static org.deuce.benchmark.Barrier benchBarrier5;
	@Bootstrap(id = 8)
	static org.deuce.benchmark.Barrier benchBarrier6;
	@Bootstrap(id = 9)
	static Genome g;
	@Bootstrap(id = 10)
	static ByteString gene;

	@Atomic
	public static void initBarriers()
	{
		if (setupBarrier == null)
			setupBarrier = new org.deuce.benchmark.Barrier(
					Integer.getInteger("tribu.replicas"));
		if (finishBarrier == null)
			finishBarrier = new org.deuce.benchmark.Barrier(
					Integer.getInteger("tribu.replicas"));
	}

	@Atomic
	public static void initBenchBarriers()
	{
		int expected = g.numThread * Integer.getInteger("tribu.replicas");
		benchBarrier1 = new org.deuce.benchmark.Barrier(expected);
		benchBarrier2 = new org.deuce.benchmark.Barrier(expected);
		benchBarrier3 = new org.deuce.benchmark.Barrier(expected);
		benchBarrier4 = new org.deuce.benchmark.Barrier(expected);
		benchBarrier5 = new org.deuce.benchmark.Barrier(expected);
		benchBarrier6 = new org.deuce.benchmark.Barrier(expected);
	}

	@Atomic
	public static void initGene()
	{
		gene = g.genePtr.contents;
	}

	// @Atomic
	// private static void waitForGenome() {
	// if (g == null)
	// throw new TransactionException("Waiting for Genome");
	// }

	@Atomic
	private static void initGenome(String[] x)
	{
		g = new Genome(x);
	}

	int geneLength;
	int segmentLength;
	int minNumSegment;
	int numThread;

	int threadid;

	// add segments, random, etc to member variables
	// include in constructor
	// allows for passing in thread run function
	Random randomPtr;
	Gene genePtr;
	Segments segmentsPtr;
	Sequencer sequencerPtr;

	Genome(String x[])
	{
		parseCmdLine(x);
		if (numThread == 0)
		{
			numThread = 1;
		}

		randomPtr = new Random();
		randomPtr.random_alloc();
		randomPtr.random_seed(0);

		// genePtr = new Gene(geneLength);
		// genePtr.create(randomPtr);
		//
		// segmentsPtr = new Segments(segmentLength, minNumSegment);
		// segmentsPtr.create(genePtr, randomPtr);
		//
		// sequencerPtr = new Sequencer(geneLength, segmentLength, segmentsPtr);
	}

	@Atomic
	public void initGenomeGene()
	{
		genePtr = new Gene(geneLength);
		genePtr.create(randomPtr);
	}

	@Atomic
	public void initGenomeSegments()
	{
		segmentsPtr = new Segments(segmentLength, minNumSegment);
	}

	@Atomic
	public void createGenomeSegments()
	{
		segmentsPtr.create(genePtr, randomPtr);
	}

	@Atomic
	public void initGenomeSequencer()
	{
		sequencerPtr = new Sequencer(geneLength, segmentLength, segmentsPtr);
	}

	Genome(int myThreadid, int myGeneLength, int mySegLength, int myMinNumSegs,
			int myNumThread, Random myRandomPtr, Gene myGenePtr,
			Segments mySegmentsPtr, Sequencer mySequencerPtr)
	{
		threadid = myThreadid;
		geneLength = myGeneLength;
		segmentLength = mySegLength;
		minNumSegment = myMinNumSegs;
		numThread = myNumThread;

		randomPtr = myRandomPtr;
		genePtr = myGenePtr;
		segmentsPtr = mySegmentsPtr;
		sequencerPtr = mySequencerPtr;
	}

	public void parseCmdLine(String args[])
	{
		int i = 0;
		String arg;
		while (i < args.length && args[i].startsWith("-"))
		{
			arg = args[i++];
			// check options
			if (arg.equals("-g"))
			{
				if (i < args.length)
				{
					this.geneLength = new Integer(args[i++]).intValue();
				}
			}
			else if (arg.equals("-s"))
			{
				if (i < args.length)
				{
					this.segmentLength = new Integer(args[i++]).intValue();
				}
			}
			else if (arg.equals("-n"))
			{
				if (i < args.length)
				{
					this.minNumSegment = new Integer(args[i++]).intValue();
				}
			}
			else if (arg.equals("-t"))
			{
				if (i < args.length)
				{
					this.numThread = new Integer(args[i++]).intValue();
				}
			}
		}

	}

	public void run()
	{
		Barrier.enterBarrier();
		Sequencer.run(threadid,
				g.numThread * Integer.getInteger("tribu.replicas"), randomPtr,
				sequencerPtr);
		Barrier.enterBarrier();
	}

	public static void main(String x[])
	{
		if (Integer.getInteger("tribu.site") == 1)
		{
			System.out.print("Creating gene and segments... ");
			initGenome(x);
			g.initGenomeGene();
			g.initGenomeSegments();
			g.createGenomeSegments();
			g.initGenomeSequencer();
			System.out.println("done.");
			initBenchBarriers();
			initGene();

			System.out.println("Gene length     = " + g.genePtr.length);
			System.out.println("Segment length  = " + g.segmentsPtr.length);
			System.out.println("Number segments = "
					+ g.segmentsPtr.contentsPtr.size());
			System.out.println("Number threads  = " + g.numThread);
		}

		initBarriers();
		setupBarrier.join();

		Barrier.setBarrier(g.numThread);

		/* Create and Start Threads */

		// ByteString gene = g.genePtr.contents;
		Genome[] gn = new Genome[g.numThread];

		for (int i = 1; i < g.numThread; i++)
		{
			int id = (Integer.getInteger("tribu.site") - 1) * g.numThread + i;
			gn[i] = new Genome(id, g.geneLength, g.segmentLength,
					g.minNumSegment, g.numThread
							* Integer.getInteger("tribu.replicas"),
					g.randomPtr, g.genePtr, g.segmentsPtr, g.sequencerPtr);
		}

		System.out.println("Sequencing gene... ");

		for (int i = 1; i < g.numThread; i++)
		{
			gn[i].start();
		}

		Profiler.enabled = true;
		long start = System.currentTimeMillis();
		Barrier.enterBarrier();
		Sequencer.run((Integer.getInteger("tribu.site") - 1) * g.numThread + 0,
				g.numThread * Integer.getInteger("tribu.replicas"),
				g.randomPtr, g.sequencerPtr);
		Barrier.enterBarrier();

		long stop = System.currentTimeMillis();
		long diff = stop - start;
		System.out.println("TIME=" + diff);

		finishBarrier.join();
		stop = System.currentTimeMillis();
		diff = stop - start;
		System.out.println("TIME2=" + diff);

		System.out.println("done.");

		Profiler.print();

		/* Check result */
		{
			ByteString sequence = g.sequencerPtr.sequence;
			boolean result = gene.compareTo(sequence) == 0;
			System.out.println("Sequence matches gene: "
					+ (result ? "yes" : "no"));
			// DEBUG
			if (result)
			{
				System.out.println("gene     = " + gene);
				System.out.println("sequence = " + sequence);
			}
		}

		TribuDSTM.close();
	}
}
