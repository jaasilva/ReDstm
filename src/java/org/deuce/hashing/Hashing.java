package org.deuce.hashing;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public interface Hashing
{
	/**
	 * @param str
	 * @param buckets
	 * @return
	 */
	public int consistentHash(String str, int buckets);

	/**
	 * @param arr
	 * @param buckets
	 * @return
	 */
	public int consistentHash(byte[] arr, int buckets);
}
