package jstamp.vacation;

import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.Atomic;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.Bootstrap;
import org.deuce.profiling.PRProfiler;

/*
 * =============================================================================
 * vacation.c
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

public class Vacation
{
	public Vacation()
	{
	}

	public static void displayUsage(String appName)
	{
		System.out.println("Usage: %s [options]\n" + appName);
		System.out
				.println("\nOptions:                                             (defaults)\n");
		System.out
				.println("    c <UINT>   Number of [c]lients                   (%i)\n"
						+ Defines.PARAM_DEFAULT_CLIENTS);
		System.out
				.println("    n <UINT>   [n]umber of user queries/transaction  (%i)\n"
						+ Defines.PARAM_DEFAULT_NUMBER);
		System.out
				.println("    q <UINT>   Percentage of relations [q]ueried     (%i)\n"
						+ Defines.PARAM_DEFAULT_QUERIES);
		System.out
				.println("    r <UINT>   Number of possible [r]elations        (%i)\n"
						+ Defines.PARAM_DEFAULT_RELATIONS);
		System.out
				.println("    t <UINT>   Number of [t]ransactions              (%i)\n"
						+ Defines.PARAM_DEFAULT_TRANSACTIONS);
		System.out
				.println("    u <UINT>   Percentage of [u]ser transactions     (%i)\n"
						+ Defines.PARAM_DEFAULT_USER);
		System.exit(1);
	}

	int CLIENTS;
	int NUMBER;
	int QUERIES;
	int RELATIONS;
	int TRANSACTIONS;
	int USER;
	int USER_CONSULT;

	public void setDefaultParams()
	{
		CLIENTS = Defines.PARAM_DEFAULT_CLIENTS;
		NUMBER = Defines.PARAM_DEFAULT_NUMBER;
		QUERIES = Defines.PARAM_DEFAULT_QUERIES;
		RELATIONS = Defines.PARAM_DEFAULT_RELATIONS;
		TRANSACTIONS = Defines.PARAM_DEFAULT_TRANSACTIONS;
		USER = Defines.PARAM_DEFAULT_USER;
		USER_CONSULT = Defines.PARAM_DEFAULT_USER_CONSULT;
	}

	public void parseArgs(String argv[])
	{
		int opterr = 0;
		setDefaultParams();
		for (int i = 0; i < argv.length; i++)
		{
			String arg = argv[i];
			if (arg.equals("-c"))
				CLIENTS = Integer.parseInt(argv[++i]);
			else if (arg.equals("-n"))
				NUMBER = Integer.parseInt(argv[++i]);
			else if (arg.equals("-q"))
				QUERIES = Integer.parseInt(argv[++i]);
			else if (arg.equals("-r"))
				RELATIONS = Integer.parseInt(argv[++i]);
			else if (arg.equals("-t"))
				TRANSACTIONS = Integer.parseInt(argv[++i]);
			else if (arg.equals("-u"))
				USER = Integer.parseInt(argv[++i]);
			else if (arg.equals("-uc"))
				USER_CONSULT = Integer.parseInt(argv[++i]);
			else
				opterr++;
		}

		if (opterr > 0)
		{
			displayUsage(argv[0]);
		}
	}

	public static boolean addCustomer(Manager managerPtr, int id, int num,
			int price)
	{
		return managerPtr.manager_addCustomer(id);
	}

	@Bootstrap(id = 5)
	static int[] ids;

	@Atomic
	public void initializeManager(int numRelations)
	{
		System.out.println("Initializing manager... ");
		managerPtr = new Manager();
		ids = new int[numRelations];
	}

	@Atomic
	public final void initIds(final int begin, final int end)
	{
		final int[] arr = ids;
		for (int i = begin; i < end; i++)
		{
			arr[i] = i + 1;
		}
	}

	@Atomic
	public final void shuffleIds(final int begin, final int end,
			final Random randomPtr, int numRelations, int base)
	{
		final int[] arr = ids;
		for (int i = begin; i < end; i++)
		{
			int x = base + (randomPtr.posrandom_generate() % numRelations);
			int y = base + (randomPtr.posrandom_generate() % numRelations);
			int tmp = arr[x];
			arr[x] = arr[y];
			arr[y] = tmp;
		}
	}

	public void initManager(int numRelations)
	{
		int i;
		System.out.println("Initializing ids... ");

		Random randomPtr = new Random();
		randomPtr.random_alloc();

		int chunk = numRelations / 2;
		for (i = 0; i < numRelations; i += chunk + 1)
		{
			int end = i + chunk;
			end = (end > numRelations ? numRelations : end);
			initIds(i, end);
		}
	}

	// @Atomic
	public void populateManager(int base, int numRelations)
	{
		int i;
		int t;
		System.out.println("Populating manager... ");

		Random randomPtr = new Random();
		randomPtr.random_alloc();

		for (t = 0; t < 4; t++)
		{
			/* Shuffle ids */
			int chunk = numRelations / 2;
			for (i = base; i < base + numRelations; i += chunk)
			{
				int end = i + chunk;
				end = (end > base + end ? base + end : end);
				shuffleIds(i, end, randomPtr, numRelations, base);
			}

			/* Populate table */
			chunk = numRelations / 8;
			for (i = base; i < base + numRelations; i += chunk)
			{
				int end = i + chunk;
				end = (end > base + end ? base + end : end);
				populateTable(i, end, randomPtr, t);
			}
		}
		System.out.println("\ndone.");
	}

	@Atomic
	public void populateTable(int i, int end, Random randomPtr, int t)
	{
		final int[] arr = ids;
		for (; i < end; i++)
		{ // Populate table
			int id = arr[i];
			int num = ((randomPtr.posrandom_generate() % 5) + 1) * 100;
			int price = ((randomPtr.posrandom_generate() % 5) * 10) + 50;
			if (t == 0)
			{
				managerPtr.manager_addCar(id, num, price);
			}
			else if (t == 1)
			{
				managerPtr.manager_addFlight(id, num, price);
			}
			else if (t == 2)
			{
				managerPtr.manager_addRoom(id, num, price);
			}
			else if (t == 3)
			{
				managerPtr.manager_addCustomer(id);
			}
		}
	}

	public Client[] initializeClients(Manager managerPtr)
	{
		Random randomPtr;
		Client clients[];
		int i;
		int numClient = CLIENTS;
		int numTransaction = TRANSACTIONS;
		int numTransactionPerClient;
		int numQueryPerTransaction = NUMBER;
		int numRelation = RELATIONS;
		int percentQuery = QUERIES;
		int queryRange;
		int percentUser = USER;
		int percentConsult = USER_CONSULT;

		System.out.println("Initializing clients... ");

		randomPtr = new Random();
		randomPtr.random_alloc();

		clients = new Client[numClient];

		numTransactionPerClient = (int) ((double) numTransaction
				/ (double) numClient + 0.5);
		queryRange = (int) ((double) percentQuery / 100.0
				* (double) numRelation + 0.5);

		for (i = 0; i < numClient; i++)
		{
			clients[i] = new Client((Integer.getInteger("tribu.site") - 1)
					* numClient + i, managerPtr, numTransactionPerClient,
					numQueryPerTransaction, queryRange, percentUser,
					percentConsult);
		}

		System.out.println("done.");
		System.out.println("    Transactions        = " + numTransaction);
		System.out.println("    Clients             = " + numClient);
		System.out.println("    Transactions/client = "
				+ numTransactionPerClient);
		System.out.println("    Queries/transaction = "
				+ numQueryPerTransaction);
		System.out.println("    Relations           = " + numRelation);
		System.out.println("    Query percent       = " + percentQuery);
		System.out.println("    Query range         = " + queryRange);
		System.out.println("    Percent user        = " + percentUser + " ("
				+ percentConsult + " read-only)");

		return clients;
	}

	@Bootstrap(id = 0)
	static public org.deuce.benchmark.Barrier firstBarrier;
	@Bootstrap(id = 1)
	static public org.deuce.benchmark.Barrier setupBarrier;
	@Bootstrap(id = 2)
	static public org.deuce.benchmark.Barrier finishBarrier;
	@Bootstrap(id = 3)
	static org.deuce.benchmark.Barrier benchBarrier;
	@Bootstrap(id = 4)
	static Manager managerPtr;
	@Bootstrap(id = 2000)
	static public org.deuce.benchmark.Barrier exitBarrier;

	@Atomic
	private static void initBarriers()
	{
		if (firstBarrier == null)
			firstBarrier = new org.deuce.benchmark.Barrier(
					Integer.getInteger("tribu.replicas"));
		if (setupBarrier == null)
			setupBarrier = new org.deuce.benchmark.Barrier(
					Integer.getInteger("tribu.replicas"));
		if (finishBarrier == null)
			finishBarrier = new org.deuce.benchmark.Barrier(
					Integer.getInteger("tribu.replicas"));
		if (exitBarrier == null)
			exitBarrier = new org.deuce.benchmark.Barrier(
					Integer.getInteger("tribu.replicas"));
	}

	@Atomic
	private static void initBenchBarrier(int numThreads)
	{
		if (benchBarrier == null)
			benchBarrier = new org.deuce.benchmark.Barrier(numThreads);
	}

	public static int MAX, MIN;
	public static AtomicInteger reservations = new AtomicInteger(0),
			deleteCustomers = new AtomicInteger(0),
			updateTables = new AtomicInteger(0),
			consults = new AtomicInteger(0);

	public static void main(String argv[]) throws Exception
	{
		final int PR_GROUP_ID = TribuDSTM.getLocalGroup().getId();
		final int NUM_GROUPS = TribuDSTM.getNumGroups();
		final boolean IS_GROUP_MASTER = TribuDSTM.isGroupMaster();
		final int SITE = Integer.getInteger("tribu.site");

		System.out.println("------------------------------------------------");
		System.out.println("------------------------------------------------");
		System.out.println("NODE: " + SITE + " | GROUP: " + PR_GROUP_ID
				+ " of " + (NUM_GROUPS - 1));
		System.out.println("AM I THE GROUP MASTER: " + IS_GROUP_MASTER);
		System.out.println("------------------------------------------------");
		System.out.println("------------------------------------------------");

		Client clients[];
		long start;
		long stop;

		/* Initialization */
		Vacation vac = new Vacation();
		vac.parseArgs(argv);

		if (Integer.getInteger("tribu.site") == 1)
		{
			vac.initializeManager(vac.RELATIONS);
			vac.initManager(vac.RELATIONS);
		}

		initBarriers();

		System.err.println("### firstBarrier");
		firstBarrier.join();

		int sections = vac.RELATIONS;
		int firstSection = 0;
		int rest = 0;

		if (NUM_GROUPS == 1)
		{
			firstSection = vac.RELATIONS;
		}
		else
		{
			sections = (int) Math.floor(vac.RELATIONS / NUM_GROUPS);
			rest = vac.RELATIONS - (NUM_GROUPS * sections);
			firstSection = sections + rest;
		}
		int size = PR_GROUP_ID == 0 ? firstSection : sections;
		int base = PR_GROUP_ID == 0 ? PR_GROUP_ID * size : PR_GROUP_ID * size
				+ rest;

		MIN = base;
		MAX = base + size;
		System.out.println("base=" + base + " size=" + size + " [" + MIN + ", "
				+ MAX + "[");

		if (IS_GROUP_MASTER) // all group masters
		{
			vac.populateManager(base, size);
			initBenchBarrier(vac.CLIENTS * Integer.getInteger("tribu.replicas"));
		}

		System.err.println("### setupBarrier");
		setupBarrier.join();

		clients = vac.initializeClients(managerPtr);
		int numThread = vac.CLIENTS;

		/* Run transactions */
		System.out.println("Running clients... ");

		Barrier.setBarrier(numThread + 1);

		for (int i = 0; i < numThread; i++)
		{
			clients[i].start();
		}

		Barrier.enterBarrier();
		start = System.currentTimeMillis();
		Barrier.enterBarrier();
		stop = System.currentTimeMillis();

		Barrier.assertIsClear();

		System.out.println("done.");
		long diff = stop - start;
		System.out.println("TIME=" + diff);

		System.err.println("### finishBarrier");
		finishBarrier.join();
		stop = System.currentTimeMillis();
		PRProfiler.enabled = false;
		diff = stop - start;
		System.out.println("TIME2=" + diff);

		System.out.println();
		System.out.println("RESULTS:");
		System.out.println(" Test duration (ms) = " + diff);
		System.out.println(" Stats:");
		System.out.println(" R=" + reservations + " C=" + consults + " D="
				+ deleteCustomers + " U=" + updateTables);
		System.out.println();

		if (Integer.getInteger("tribu.site") == 1)
		{
			vac.checkTables(managerPtr);
		}

		System.err.println("### exitBarrier");
		exitBarrier.join();

		PRProfiler.print();

		TribuDSTM.close();
	}

	@Atomic
	void checkTables(Manager managerPtr)
	{
		int i;
		int numRelation = RELATIONS;
		RBTree customerTablePtr = managerPtr.customerTablePtr;
		RBTree tables[] = new RBTree[3];
		tables[0] = managerPtr.carTablePtr;
		tables[1] = managerPtr.flightTablePtr;
		tables[2] = managerPtr.roomTablePtr;
		int numTable = 3;
		int t;

		System.out.println("Checking tables... ");
		for (t = 0; t < 4; t++)
		{
			switch (t)
			{
			case 0:
				System.out.println("Verifying cars...");
				break;
			case 1:
				System.out.println("Verifying flights...");
				break;
			case 2:
				System.out.println("Verifying rooms...");
				break;
			case 3:
				System.out.println("Verifying customers...");
				break;
			}
			final RBTree tree = (t < 3 ? tables[t]
					: managerPtr.customerTablePtr);
			tree.verify(1);
		}

		/* Check for unique customer IDs */
		int percentQuery = QUERIES;
		int queryRange = (int) ((double) percentQuery / 100.0
				* (double) numRelation + 0.5);
		int maxCustomerId = queryRange + 1;
		for (i = 1; i <= maxCustomerId; i++)
		{
			if (customerTablePtr.find(i) != null)
			{
				customerTablePtr.remove(i);
			}
		}

		/* Check reservation tables for consistency and unique ids */
		for (t = 0; t < numTable; t++)
		{
			RBTree tablePtr = tables[t];
			for (i = 1; i <= numRelation; i++)
			{
				if (tablePtr.find(i) != null)
				{
					if (t == 0)
					{
						managerPtr.manager_addCar(i, 0, 0);
					}
					else if (t == 1)
					{
						managerPtr.manager_addFlight(i, 0, 0);
					}
					else if (t == 2)
					{
						managerPtr.manager_addRoom(i, 0, 0);
					}
					tablePtr.remove(i);
				}
			}
		}
		System.out.println("done.");
	}
}
