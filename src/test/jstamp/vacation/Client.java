package jstamp.vacation;

import org.deuce.Atomic;
import org.deuce.profiling.Profiler;

/*
 * =============================================================================
 * client.c
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

public class Client extends Thread
{
	private int id;
	private Manager managerPtr;
	private Random randomPtr;
	private int numOperation;
	private int numQueryPerTransaction;
	private int queryRange;
	private int percentUser;
	private int percentConsult;
	private final java.util.Random rand = new java.util.Random();

	public Client()
	{
	}

	/**
	 * Returns NULL on failure
	 */
	public Client(int id, Manager managerPtr, int numOperation,
			int numQueryPerTransaction, int queryRange, int percentUser,
			int percentConsult)
	{
		this.randomPtr = new Random();
		this.randomPtr.random_alloc();

		this.id = id;
		this.managerPtr = managerPtr;
		this.numOperation = numOperation;
		this.numQueryPerTransaction = numQueryPerTransaction;
		this.queryRange = queryRange;
		this.percentUser = percentUser;
		this.percentConsult = percentConsult;
	}

	public int selectAction(int r, int percentUser)
	{
		if (r < percentUser)
		{
			int c = randomPtr.posrandom_generate() % 100;
			if (c < percentConsult)
			{
				return Defines.ACTION_CONSULT;
			}
			else
			{
				return Defines.ACTION_MAKE_RESERVATION;
			}
		}
		else if ((r & 1) == 1)
		{
			return Defines.ACTION_DELETE_CUSTOMER;
		}
		else
		{
			return Defines.ACTION_UPDATE_TABLES;
		}
	}

	/**
	 * Execute list operations on the database
	 */
	public void run()
	{
		System.err.println("### benchBarrier: " + id);
		try
		{
			Thread.sleep(rand.nextInt(3000));
		}
		catch (InterruptedException e)
		{
		}
		Vacation.benchBarrier.join();

		Profiler.enable();
		Barrier.enterBarrier();
		for (int i = 0; i < numOperation; i++)
		{
			int r = randomPtr.posrandom_generate() % 100;
			int action = selectAction(r, percentUser);

			if (action == Defines.ACTION_CONSULT)
			{
				Vacation.consults.incrementAndGet();
				int numQuery = randomPtr.posrandom_generate()
						% numQueryPerTransaction + 1;

				consult(managerPtr, numQuery);
			}
			else if (action == Defines.ACTION_MAKE_RESERVATION)
			{
				Vacation.reservations.incrementAndGet();
				int numQuery = randomPtr.posrandom_generate()
						% numQueryPerTransaction + 1;
				int customerId = randomPtr.posrandom_generate() % queryRange
						+ 1;
				makeReservation(managerPtr, numQuery, customerId);
			}
			else if (action == Defines.ACTION_DELETE_CUSTOMER)
			{
				Vacation.deleteCustomers.incrementAndGet();
				int customerId = randomPtr.posrandom_generate() % queryRange
						+ 1;
				deleteCustomer(managerPtr, customerId);
			}
			else if (action == Defines.ACTION_UPDATE_TABLES)
			{
				Vacation.updateTables.incrementAndGet();
				int numUpdate = randomPtr.posrandom_generate()
						% numQueryPerTransaction + 1;
				updateTables(managerPtr, numUpdate);
			}
		}
		System.out.println("> Client " + id + " done.");
		Barrier.enterBarrier();
	}

	@Atomic
	private int updateTables(Manager managerPtr, int numUpdate)
	{
		int n;
		for (n = 0; n < numUpdate; n++)
		{
			int t = randomPtr.posrandom_generate()
					% Defines.NUM_RESERVATION_TYPE;
			int id = (randomPtr.posrandom_generate() % queryRange) + 1;
			int doAdd = randomPtr.posrandom_generate() % 2;
			if (doAdd == 1)
			{ // do add
				int newPrice = ((randomPtr.posrandom_generate() % 5) * 10) + 50;
				if (t == Defines.RESERVATION_CAR)
				{
					managerPtr.manager_addCar(id, 100, newPrice);
				}
				else if (t == Defines.RESERVATION_FLIGHT)
				{
					managerPtr.manager_addFlight(id, 100, newPrice);
				}
				else if (t == Defines.RESERVATION_ROOM)
				{
					managerPtr.manager_addRoom(id, 100, newPrice);
				}
			}
			else
			{ // do delete
				if (t == Defines.RESERVATION_CAR)
				{
					managerPtr.manager_deleteCar(id, 100);
				}
				else if (t == Defines.RESERVATION_FLIGHT)
				{
					managerPtr.manager_deleteFlight(id);
				}
				else if (t == Defines.RESERVATION_ROOM)
				{
					managerPtr.manager_deleteRoom(id, 100);
				}
			}
		}
		return n;
	}

	@Atomic
	private void deleteCustomer(Manager managerPtr, int customerId)
	{
		int bill = managerPtr.manager_queryCustomerBill(customerId);
		if (bill >= 0)
		{
			managerPtr.manager_deleteCustomer(customerId);
		}
	}

	@Atomic
	private int makeReservation(Manager managerPtr, int numQuery, int customerId)
	{
		int n;
		boolean isFound = false;
		int maxPrices_car = -1, maxPrices_flight = -1, maxPrices_room = -1;
		int maxIds_car = -1, maxIds_flight = -1, maxIds_room = -1;
		for (n = 0; n < numQuery; n++)
		{
			int t = randomPtr.random_generate() % Defines.NUM_RESERVATION_TYPE;
			int id = (randomPtr.random_generate() % queryRange) + 1;
			int price = -1;
			if (t == Defines.RESERVATION_CAR)
			{
				if (managerPtr.manager_queryCar(id) >= 0)
				{
					price = managerPtr.manager_queryCarPrice(id);
				}
				if (price > maxPrices_car)
				{
					maxPrices_car = price;
					maxIds_car = id;
					isFound = true;
				}
			}
			else if (t == Defines.RESERVATION_FLIGHT)
			{
				if (managerPtr.manager_queryFlight(id) >= 0)
				{
					price = managerPtr.manager_queryFlightPrice(id);
				}
				if (price > maxPrices_flight)
				{
					maxPrices_flight = price;
					maxIds_flight = id;
					isFound = true;
				}
			}
			else if (t == Defines.RESERVATION_ROOM)
			{
				if (managerPtr.manager_queryRoom(id) >= 0)
				{
					price = managerPtr.manager_queryRoomPrice(id);
				}
				if (price > maxPrices_room)
				{
					maxPrices_room = price;
					maxIds_room = id;
					isFound = true;
				}
			}
		}
		if (isFound)
		{
			managerPtr.manager_addCustomer(customerId);
		}
		if (maxIds_car > 0)
		{
			managerPtr.manager_reserveCar(customerId, maxIds_car);
		}
		if (maxIds_flight > 0)
		{
			managerPtr.manager_reserveFlight(customerId, maxIds_flight);
		}
		if (maxIds_room > 0)
		{
			managerPtr.manager_reserveRoom(customerId, maxIds_room);
		}
		return n;
	}

	@Atomic
	private int consult(Manager managerPtr, int numQuery)
	{
		int n;
		for (n = 0; n < numQuery; n++)
		{
			int t = randomPtr.random_generate() % Defines.NUM_RESERVATION_TYPE;
			int id = (randomPtr.random_generate() % queryRange) + 1;

			if (t == Defines.RESERVATION_CAR)
			{
				if (managerPtr.manager_queryCar(id) >= 0)
				{
					managerPtr.manager_queryCarPrice(id);
				}
			}
			else if (t == Defines.RESERVATION_FLIGHT)
			{
				if (managerPtr.manager_queryFlight(id) >= 0)
				{
					managerPtr.manager_queryFlightPrice(id);
				}
			}
			else if (t == Defines.RESERVATION_ROOM)
			{
				if (managerPtr.manager_queryRoom(id) >= 0)
				{
					managerPtr.manager_queryRoomPrice(id);
				}
			}
		}
		return n;
	}
}
