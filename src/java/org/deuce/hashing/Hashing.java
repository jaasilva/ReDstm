package org.deuce.hashing;

/**
 * @author jaasilva
 * 
 */
public interface Hashing
{
	public int consistentHash(String str, int buckets);
}
