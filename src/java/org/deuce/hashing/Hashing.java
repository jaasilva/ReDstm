package org.deuce.hashing;

import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public interface Hashing
{
	public int consistentHash(String str, int buckets);

	public int consistentHash(byte[] arr, int buckets);
}
