package org.deuce.transaction.pool;

public interface ResourceFactory<T>
{
	T newInstance();
}
