package org.deuce.transaction.score2.pool;

public interface ResourceFactory<T>
{
	T newInstance();
}
