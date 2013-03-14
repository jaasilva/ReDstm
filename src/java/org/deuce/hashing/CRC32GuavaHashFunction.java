package org.deuce.hashing;

import com.google.common.hash.Hashing;

/**
 * @author jaasilva
 * 
 */
public class CRC32GuavaHashFunction implements org.deuce.hashing.Hashing
{
	/*
	 * (non-Javadoc)
	 * @see org.deuce.hashing.Hashing#consistentHash(java.lang.String, int)
	 */
	@Override
	public int consistentHash(String str, int buckets)
	{
		return Hashing.consistentHash(Hashing.crc32().hashString(str), buckets);
	}

}
