package org.deuce.hashing;

import com.google.common.hash.Hashing;

/**
 * @author jaasilva
 * 
 */
public class SHA1GuavaHashFunction implements org.deuce.hashing.Hashing
{
	/*
	 * (non-Javadoc)
	 * @see org.deuce.hashing.Hashing#consistentHash(java.lang.String, int)
	 */
	@Override
	public int consistentHash(String str, int buckets)
	{
		return Hashing.consistentHash(Hashing.sha1().hashString(str), buckets);
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.hashing.Hashing#consistentHash(byte[], int)
	 */
	@Override
	public int consistentHash(byte[] arr, int buckets)
	{
		return Hashing.consistentHash(Hashing.sha1().hashBytes(arr), buckets);
	}

}
