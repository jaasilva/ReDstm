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
	private int CLIENTS;
	private int NUMBER;
	private int QUERIES;
	private int RELATIONS;
	private int TRANSACTIONS;
	private int USER;
	private int USER_CONSULT;

	@Bootstrap(id = 0)
	public static org.deuce.benchmark.Barrier firstBarrier;
	@Bootstrap(id = 1)
	public static org.deuce.benchmark.Barrier setupBarrier;
	@Bootstrap(id = 2)
	public static org.deuce.benchmark.Barrier finishBarrier;
	@Bootstrap(id = 3)
	public static org.deuce.benchmark.Barrier benchBarrier;
	@Bootstrap(id = 2000)
	public static org.deuce.benchmark.Barrier exitBarrier;
	@Bootstrap(id = 2001)
	public static org.deuce.benchmark.Barrier shuffleBarrier;
	@Bootstrap(id = 4)
	public static Manager managerPtr;
	@Bootstrap(id = 5)
	public static int[] ids;

	public static int MAX, MIN;
	public static AtomicInteger reservations = new AtomicInteger(0),
			deleteCustomers = new AtomicInteger(0),
			updateTables = new AtomicInteger(0),
			consults = new AtomicInteger(0);
	private static boolean _partial = TribuDSTM.PARTIAL;

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

	@Atomic
	public void initializeManager(int numRelations)
	{
		System.out.println("Initializing manager... ");
		managerPtr = new Manager();

		Random randomPtr = new Random();
		randomPtr.random_alloc();

		ids = new int[numRelations];
		int chunk = numRelations / 8; // Initializing ids
		for (int i = 0; i < numRelations; i += chunk)
		{
			int end = i + chunk;
			end = (end > numRelations ? numRelations : end);
			initIds(i, end);
		}
	}

	@Atomic
	public final void initIds(final int begin, final int end)
	{
		for (int i = begin; i < end; i++)
		{
			ids[i] = i + 1;
		}
	}

	public void populateManager(int base, int numRelations, int pr_group_id,
			int totalRelations)
	{
		System.out.println("Populating manager...");

		Random randomPtr = new Random();
		randomPtr.random_alloc();

		for (int t = 0; t < 4; t++)
		{
			if (pr_group_id == 0)
			{ // Shuffle ids
				shuffleIds(totalRelations, randomPtr);
			}
			shuffleBarrier.join();

			/* Populate table */
			int chunk = numRelations / 2;
			for (int i = base; i < base + numRelations; i += chunk)
			{
				int end = i + chunk;
				end = (end > base + numRelations ? base + numRelations : end);
				populateTable(i, end, randomPtr, t);
			}
		}
		System.out.println("done.");
	}

	@Atomic
	public final void shuffleIds(int totalRelations, final Random randomPtr)
	{
		System.out.println("Suffling ids...");
		int numRelations = totalRelations;
		for (int i = 0; i < 2 * numRelations; i++)
		{
			int x = randomPtr.posrandom_generate() % numRelations;
			int y = randomPtr.posrandom_generate() % numRelations;
			int tmp = ids[x];
			ids[x] = ids[y];
			ids[y] = tmp;
		}
	}

	@Atomic
	public void populateTable(int begin, int end, Random randomPtr, int table)
	{ // Populate table
		for (int i = begin; i < end; i++)
		{
			int id = ids[i];
			int num = ((randomPtr.posrandom_generate() % 5) + 1) * 100;
			int price = ((randomPtr.posrandom_generate() % 5) * 10) + 50;
			switch (table)
			{
			case 0:
				managerPtr.manager_addCar(id, num, price);
				break;
			case 1:
				managerPtr.manager_addFlight(id, num, price);
				break;
			case 2:
				managerPtr.manager_addRoom(id, num, price);
				break;
			case 3:
				managerPtr.manager_addCustomer(id);
			}
		}
	}

	public Client[] initializeClients(Manager managerPtr)
	{
		int numClients = CLIENTS;
		int numTxs = TRANSACTIONS;
		int numQueryPerTx = NUMBER;
		int numRelations = RELATIONS;
		int percentQuery = QUERIES;
		int percentUser = USER;
		int percentConsult = USER_CONSULT;

		System.out.println("Initializing clients... ");

		Random randomPtr = new Random();
		randomPtr.random_alloc();

		Client clients[] = new Client[numClients];

		int numTxsPerClient = (int) ((double) numTxs / (double) numClients + 0.5);
		int queryRange = (int) ((double) percentQuery / 100.0
				* (double) numRelations + 0.5);

		for (int i = 0; i < numClients; i++)
		{
			clients[i] = new Client((Integer.getInteger("tribu.site") - 1)
					* numClients + i, managerPtr, numTxsPerClient,
					numQueryPerTx, queryRange, percentUser, percentConsult);
		}

		System.out.println("done.");
		System.out.println("  Transactions        = " + numTxs);
		System.out.println("  Clients             = " + numClients);
		System.out.println("  Transactions/client = " + numTxsPerClient);
		System.out.println("  Queries/transaction = " + numQueryPerTx);
		System.out.println("  Relations           = " + numRelations);
		System.out.println("  Query percent       = " + percentQuery);
		System.out.println("  Query range         = " + queryRange);
		System.out.println("  Percent user        = " + percentUser + " ("
				+ percentConsult + " read-only)");

		return clients;
	}

	@Atomic
	private static void initBarriers(int replicas, int groups, int numThreads)
	{
		if (shuffleBarrier == null)
			shuffleBarrier = new org.deuce.benchmark.Barrier(groups);
		if (setupBarrier == null)
			setupBarrier = new org.deuce.benchmark.Barrier(replicas);
		if (finishBarrier == null)
			finishBarrier = new org.deuce.benchmark.Barrier(replicas);
		if (exitBarrier == null)
			exitBarrier = new org.deuce.benchmark.Barrier(replicas);
		if (benchBarrier == null)
			benchBarrier = new org.deuce.benchmark.Barrier(numThreads);
	}

	@Atomic
	private static void initFirstBarrier(int replicas)
	{
		if (firstBarrier == null)
			firstBarrier = new org.deuce.benchmark.Barrier(replicas);
	}

	public static void main(String argv[]) throws Exception
	{
		final int PR_GROUP_ID;
		final int NUM_GROUPS;
		final boolean IS_GROUP_MASTER;
		final int SITE = Integer.getInteger("tribu.site");

		if (_partial)
		{ // running in partial rep. mode
			PR_GROUP_ID = TribuDSTM.getLocalGroup().getId();
			NUM_GROUPS = TribuDSTM.getNumGroups();
			IS_GROUP_MASTER = TribuDSTM.isGroupMaster();

			System.out.println("---------------------------------------------");
			System.out.println("NODE: " + SITE + " | GROUP: " + PR_GROUP_ID
					+ " of " + (NUM_GROUPS - 1));
			System.out.println("GROUP MASTER: " + IS_GROUP_MASTER);
			System.out.println("---------------------------------------------");
		}
		else
		{ // running in full rep. mode
			PR_GROUP_ID = 0;
			NUM_GROUPS = 1;
			IS_GROUP_MASTER = false;
		}

		/* Initialization */
		Vacation vac = new Vacation();
		vac.parseArgs(argv);
		int replicas = Integer.getInteger("tribu.replicas");

		if (SITE == 1)
		{
			vac.initializeManager(vac.RELATIONS);
			int numThreads = vac.CLIENTS * replicas;
			initBarriers(replicas, NUM_GROUPS, numThreads);
		}

		initFirstBarrier(replicas);
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
		int base = PR_GROUP_ID == 0 ? 0 : PR_GROUP_ID * size + rest;

		MIN = base;
		MAX = base + size;
		System.out.println("base=" + base + " size=" + size + " [" + MIN + ", "
				+ MAX + "[");

		if ((_partial && IS_GROUP_MASTER) /* all par. rep. group masters */
				|| (!_partial && SITE == 1) /* the full rep. master */)
		{
			vac.populateManager(base, size, PR_GROUP_ID, vac.RELATIONS);
		}

		System.err.println("### setupBarrier");
		setupBarrier.join();

		Client clients[] = vac.initializeClients(managerPtr);
		int numThread = vac.CLIENTS;

		/* Run transactions */
		System.out.println("Running clients... ");

		Barrier.setBarrier(numThread + 1);
		for (int i = 0; i < numThread; i++)
		{
			clients[i].start();
		}

		Barrier.enterBarrier();
		long start = System.currentTimeMillis();
		Barrier.enterBarrier();
		long stop = System.currentTimeMillis();

		Barrier.assertIsClear();

		System.out.println("done.");
		long diff = stop - start;
		System.out.println("TIME=" + diff);

		System.err.println("### finishBarrier");
		finishBarrier.join();
		PRProfiler.enabled = false;

		stop = System.currentTimeMillis();
		diff = stop - start;
		System.out.println("TIME2=" + diff);

		System.out.println();
		System.out.println("RESULTS:");
		System.out.println(" Test duration (ms) = " + diff);
		System.out.println(" Stats:");
		System.out.println(" R=" + reservations + " C=" + consults + " D="
				+ deleteCustomers + " U=" + updateTables);
		System.out.println();

		if (SITE == 1)
		{
			vac.checkTables(managerPtr);
		}

		System.err.println("### exitBarrier");
		exitBarrier.join();

		PRProfiler.print();

		TribuDSTM.close();
	}

	@Atomic
	public void checkTables(Manager managerPtr)
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
