package org.deuce.distribution;

import org.deuce.transform.ExcludeTM;

@ExcludeTM
public interface Locator
{
	public void put(ObjectMetadata metadata, UniqueObject obj);

	public UniqueObject get(ObjectMetadata metadata);
}
