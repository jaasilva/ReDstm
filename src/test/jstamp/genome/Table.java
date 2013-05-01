package jstamp.genome;

// import java.util.LinkedList;

public class Table
{
	// LinkedList buckets[];
	TArrayList buckets[];
	int numBucket;

	/*
	 * ==========================================================================
	 * === table_alloc -- Returns NULL on failure
	 * ================================
	 * =============================================
	 */
	Table(int myNumBucket)
	{
		// buckets = new LinkedList[myNumBucket];
		buckets = new TArrayList[myNumBucket];
		for (int i = 0; i < myNumBucket; i++)
		{
			// buckets[i] = new LinkedList();
			buckets[i] = new TArrayList();
		}
		numBucket = myNumBucket;
	}

	/*
	 * ==========================================================================
	 * === table_insert -- Returns TRUE if successful, else FALSE
	 * ================
	 * =============================================================
	 */
	boolean table_insert(int hash, Object dataPtr)
	{
		int i = hash % numBucket;
		if (i < 0)
			i = -i;
		// if(buckets[i].contains(dataPtr)) {
		if (buckets[i].contains(dataPtr))
		{
			return false;
		}
		// buckets[i].add(dataPtr);
		buckets[i].add(dataPtr);
		return true;
	}

	/*
	 * ==========================================================================
	 * === table_remove -- Returns TRUE if successful, else FALSE
	 * ================
	 * =============================================================
	 */
	boolean table_remove(int hash, Object dataPtr)
	{
		int i = (hash % numBucket);
		if (i < 0)
			i = -i;
		boolean tempbool = buckets[i].contains(dataPtr);
		if (tempbool)
		{
			buckets[i].remove(dataPtr);
			return true;
		}
		return false;
	}
}
