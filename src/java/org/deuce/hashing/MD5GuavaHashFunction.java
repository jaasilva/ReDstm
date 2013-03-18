package org.deuce.hashing;

import org.deuce.transform.ExcludeTM;

import com.google.common.hash.Hashing;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
public class MD5GuavaHashFunction implements org.deuce.hashing.Hashing
{
	/*
	 * (non-Javadoc)
	 * @see org.deuce.hashing.Hashing#consistentHash(java.lang.String, int)
	 */
	@Override
	public int consistentHash(String str, int buckets)
	{
		return Hashing.consistentHash(Hashing.md5().hashString(str), buckets);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.hashing.Hashing#consistentHash(byte[], int)
	 */
	@Override
	public int consistentHash(byte[] arr, int buckets)
	{
		return Hashing.consistentHash(Hashing.md5().hashBytes(arr), buckets);
	}
}
