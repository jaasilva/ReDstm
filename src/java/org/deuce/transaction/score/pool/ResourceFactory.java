package org.deuce.transaction.score.pool;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public interface ResourceFactory<T>
{
	T newInstance();
}
