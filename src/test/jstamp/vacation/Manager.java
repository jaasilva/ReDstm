package jstamp.vacation;

/*
 * =============================================================================
 * manager.c -- Travel reservation resource manager
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

public class Manager
{
	protected RBTree carTablePtr;
	protected RBTree roomTablePtr;
	protected RBTree flightTablePtr;
	protected RBTree customerTablePtr;

	public Manager()
	{
		carTablePtr = new RBTree();
		roomTablePtr = new RBTree();
		flightTablePtr = new RBTree();
		customerTablePtr = new RBTree();
	}

	/**
	 * If 'num' > 0 then add, if < 0 remove -- Adding 0 seats is error if does
	 * not exist -- If 'price' < 0, do not update price -- Returns TRUE on
	 * success, else FALSE
	 */
	private boolean addReservation(RBTree tablePtr, int id, int num, int price)
	{
		Reservation reservationPtr = (Reservation) tablePtr.find(id);
		if (reservationPtr == null)
		{ // Create new reservation
			if (num < 1 || price < 0)
			{
				return false;
			}
			reservationPtr = new Reservation(id, num, price);
			tablePtr.insert(id, reservationPtr);
		}
		else
		{ // Update existing reservation
			if (!reservationPtr.reservation_addToTotal(num))
			{
				return false;
			}
			if (reservationPtr.numTotal == 0)
			{
				tablePtr.remove(id);
			}
			else
			{
				reservationPtr.reservation_updatePrice(price);
			}
		}

		return true;
	}

	/**
	 * Add cars to a city -- Adding to an existing car overwrite the price if
	 * 'price' >= 0 -- Returns TRUE on success, else FALSE
	 */
	public boolean manager_addCar(int carId, int numCars, int price)
	{
		return addReservation(carTablePtr, carId, numCars, price);
	}

	/**
	 * Delete cars from a city -- Decreases available car count (those not
	 * allocated to a customer) -- Fails if would make available car count
	 * negative -- If decresed to 0, deletes entire entry -- Returns TRUE on
	 * success, else FALSE
	 */
	public boolean manager_deleteCar(int carId, int numCar)
	{ // -1 keeps old price
		return addReservation(carTablePtr, carId, -numCar, -1);
	}

	/**
	 * Add rooms to a city -- Adding to an existing room overwrite the price if
	 * 'price' >= 0 -- Returns TRUE on success, else FALSE
	 */
	public boolean manager_addRoom(int roomId, int numRoom, int price)
	{
		return addReservation(roomTablePtr, roomId, numRoom, price);
	}

	/**
	 * Delete rooms from a city -- Decreases available room count (those not
	 * allocated to a customer) -- Fails if would make available room count
	 * negative -- If decreased to 0, deletes entire entry -- Returns TRUE on
	 * success, else FALSE
	 */
	public boolean manager_deleteRoom(int roomId, int numRoom)
	{ // -1 keeps old price
		return addReservation(roomTablePtr, roomId, -numRoom, -1);
	}

	/**
	 * Add seats to a flight -- Adding to an existing flight overwrite the price
	 * if 'price' >= 0 -- Returns TRUE on success, FALSE on failure
	 */
	public boolean manager_addFlight(int flightId, int numSeat, int price)
	{
		return addReservation(flightTablePtr, flightId, numSeat, price);
	}

	/**
	 * Delete an entire flight -- Fails if customer has reservation on this
	 * flight -- Returns TRUE on success, else FALSE
	 */
	public boolean manager_deleteFlight(int flightId)
	{
		Reservation reservationPtr = (Reservation) flightTablePtr
				.find(flightId);
		if (reservationPtr == null)
		{
			return false;
		}

		if (reservationPtr.numUsed > 0)
		{ // somebody has a reservation
			return false;
		}

		return addReservation(flightTablePtr, flightId,
				-reservationPtr.numTotal, -1); // -1 keeps old price
	}

	/**
	 * If customer already exists, returns failure -- Returns TRUE on success,
	 * else FALSE
	 */
	public boolean manager_addCustomer(int customerId)
	{
		if (customerTablePtr.contains(customerId))
		{
			return false;
		}

		Customer customerPtr = new Customer(customerId);
		customerTablePtr.insert(customerId, customerPtr);

		return true;
	}

	/**
	 * Delete this customer and associated reservations -- If customer does not
	 * exist, returns success -- Returns TRUE on success, else FALSE
	 */
	public boolean manager_deleteCustomer(int customerId)
	{
		Customer customerPtr;
		List_t reservationInfoListPtr;
		List_Node it;

		customerPtr = (Customer) customerTablePtr.find(customerId);
		if (customerPtr == null)
		{
			return false;
		}

		// Cancel this customer's reservations
		reservationInfoListPtr = customerPtr.reservationInfoListPtr;
		it = reservationInfoListPtr.head;
		while (it.nextPtr != null)
		{
			Reservation_Info reservationInfoPtr;
			Reservation reservationPtr = null;
			it = it.nextPtr;
			reservationInfoPtr = (Reservation_Info) it.dataPtr;
			switch (reservationInfoPtr.type)
			{
			case Defines.RESERVATION_CAR:
				reservationPtr = (Reservation) carTablePtr
						.find(reservationInfoPtr.id);
				break;
			case Defines.RESERVATION_ROOM:
				reservationPtr = (Reservation) roomTablePtr
						.find(reservationInfoPtr.id);
				break;
			case Defines.RESERVATION_FLIGHT:
				reservationPtr = (Reservation) flightTablePtr
						.find(reservationInfoPtr.id);
				break;
			}
			reservationPtr.reservation_cancel();
		}

		customerTablePtr.remove(customerId);
		return true;
	}

	/**************************************************
	 * QUERY INTERFACE
	 *************************************************/

	/**
	 * Return numFree of a reservation, -1 if failure
	 */
	private int queryNumFree(RBTree tablePtr, int id)
	{
		int numFree = -1;
		Reservation reservationPtr = (Reservation) tablePtr.find(id);
		if (reservationPtr != null)
		{
			numFree = reservationPtr.numFree;
		}

		return numFree;
	}

	/**
	 * Return price of a reservation, -1 if failure
	 */
	private int queryPrice(RBTree tablePtr, int id)
	{
		int price = -1;
		Reservation reservationPtr = (Reservation) tablePtr.find(id);
		if (reservationPtr != null)
		{
			price = reservationPtr.price;
		}

		return price;
	}

	/**
	 * Return the number of empty seats on a car -- Returns -1 if the car does
	 * not exist
	 */
	public int manager_queryCar(int carId)
	{
		return queryNumFree(carTablePtr, carId);
	}

	/**
	 * Return the price of the car -- Returns -1 if the car does not exist
	 */
	public int manager_queryCarPrice(int carId)
	{
		return queryPrice(carTablePtr, carId);
	}

	/**
	 * Return the number of empty seats on a room -- Returns -1 if the room does
	 * not exist
	 */
	public int manager_queryRoom(int roomId)
	{
		return queryNumFree(roomTablePtr, roomId);
	}

	/**
	 * Return the price of the room -- Returns -1 if the room does not exist
	 */
	public int manager_queryRoomPrice(int roomId)
	{
		return queryPrice(roomTablePtr, roomId);
	}

	/**
	 * Return the number of empty seats on a flight -- Returns -1 if the flight
	 * does not exist
	 */
	public int manager_queryFlight(int flightId)
	{
		return queryNumFree(flightTablePtr, flightId);
	}

	/**
	 * Return the price of the flight -- Returns -1 if the flight does not exist
	 */
	public int manager_queryFlightPrice(int flightId)
	{
		return queryPrice(flightTablePtr, flightId);
	}

	/**
	 * Return the total price of all reservations held for a customer -- Returns
	 * -1 if the customer does not exist
	 */
	public int manager_queryCustomerBill(int customerId)
	{
		int bill = -1;
		Customer customerPtr = (Customer) customerTablePtr.find(customerId);

		if (customerPtr != null)
		{
			bill = customerPtr.customer_getBill();
		}

		return bill;
	}

	/**************************************************
	 * RESERVATION INTERFACE
	 *************************************************/

	/**
	 * Customer is not allowed to reserve same (type, id) multiple times --
	 * Returns TRUE on success, else FALSE
	 */
	private static boolean reserve(RBTree tablePtr, RBTree customerTablePtr,
			int customerId, int id, int type)
	{
		Customer customerPtr = (Customer) customerTablePtr.find(customerId);
		if (customerPtr == null)
		{
			return false;
		}

		Reservation reservationPtr = (Reservation) tablePtr.find(id);
		if (reservationPtr == null)
		{
			return false;
		}

		if (!reservationPtr.reservation_make())
		{
			return false;
		}

		if (!customerPtr.customer_addReservationInfo(type, id,
				reservationPtr.price))
		{ // Undo previous successful reservation
			reservationPtr.reservation_cancel();
			return false;
		}

		return true;
	}

	/**
	 * Returns failure if the car or customer does not exist -- Returns TRUE on
	 * success, else FALSE
	 */
	public boolean manager_reserveCar(int customerId, int carId)
	{
		return reserve(carTablePtr, customerTablePtr, customerId, carId,
				Defines.RESERVATION_CAR);
	}

	/**
	 * Returns failure if the room or customer does not exist -- Returns TRUE on
	 * success, else FALSE
	 */
	public boolean manager_reserveRoom(int customerId, int roomId)
	{
		return reserve(roomTablePtr, customerTablePtr, customerId, roomId,
				Defines.RESERVATION_ROOM);
	}

	/**
	 * Returns failure if the flight or customer does not exist -- Returns TRUE
	 * on success, else FALSE
	 */
	public boolean manager_reserveFlight(int customerId, int flightId)
	{
		return reserve(flightTablePtr, customerTablePtr, customerId, flightId,
				Defines.RESERVATION_FLIGHT);
	}

	/**
	 * Customer is not allowed to cancel multiple times -- Returns TRUE on
	 * success, else FALSE
	 */
	private static boolean cancel(RBTree tablePtr, RBTree customerTablePtr,
			int customerId, int id, int type)
	{
		Customer customerPtr = (Customer) customerTablePtr.find(customerId);
		if (customerPtr == null)
		{
			return false;
		}

		Reservation reservationPtr = (Reservation) tablePtr.find(id);
		if (reservationPtr == null)
		{
			return false;
		}

		if (!reservationPtr.reservation_cancel())
		{
			return false;
		}

		if (!customerPtr.customer_removeReservationInfo(type, id))
		{ // Undo previous successful cancellation
			reservationPtr.reservation_make();
			return false;
		}

		return true;
	}

	/**
	 * Returns failure if the car, reservation, or customer does not exist --
	 * Returns TRUE on success, else FALSE
	 */
	public boolean manager_cancelCar(int customerId, int carId)
	{
		return cancel(carTablePtr, customerTablePtr, customerId, carId,
				Defines.RESERVATION_CAR);
	}

	/**
	 * Returns failure if the room, reservation, or customer does not exist --
	 * Returns TRUE on success, else FALSE
	 */
	public boolean manager_cancelRoom(int customerId, int roomId)
	{
		return cancel(roomTablePtr, customerTablePtr, customerId, roomId,
				Defines.RESERVATION_ROOM);
	}

	/**
	 * Returns failure if the flight, reservation, or customer does not exist --
	 * Returns TRUE on success, else FALSE
	 */
	public boolean manager_cancelFlight(int customerId, int flightId)
	{
		return cancel(flightTablePtr, customerTablePtr, customerId, flightId,
				Defines.RESERVATION_FLIGHT);
	}
}
