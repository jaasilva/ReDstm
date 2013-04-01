package org.deuce.distribution.replication.partial.protocol.score;

/**
 * @author jaasilva
 * 
 */
public class Pair<K, V>
{
	public K first;
	public V second;

	public Pair(K first, V second)
	{
		this.first = first;
		this.second = second;
	}

	@Override
	public boolean equals(Object other)
	{
		return (other instanceof Pair)
				&& (this.first.equals(((Pair<?, ?>) other).first));
	}
}
