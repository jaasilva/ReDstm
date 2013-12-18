package jstamp.vacation;

public class Reservation_Info
{
	protected int id;
	protected int type;
	protected int price;

	public Reservation_Info(int type, int id, int price)
	{
		this.type = type;
		this.id = id;
		this.price = price;
	}

	/**
	 * Returns -1 if A < B, 0 if A = B, 1 if A > B
	 */
	public static int reservation_info_compare(Reservation_Info aPtr,
			Reservation_Info bPtr)
	{
		int typeDiff = aPtr.type - bPtr.type;
		return ((typeDiff != 0) ? (typeDiff) : (aPtr.id - bPtr.id));
	}
}
