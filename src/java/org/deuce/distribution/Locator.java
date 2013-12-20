package org.deuce.distribution;

import org.deuce.transform.ExcludeTM;

/**
 * Represents the locator table. It maps distribution metadata to UniqueObjects.
 * 
 * @author tvale
 */
@ExcludeTM
public interface Locator
{
	/**
	 * Puts the new entry <K,V> in the locator table, where K is metadata and V
	 * is obj.
	 * 
	 * @param metadata - the key of the new entry in the locator table.
	 * @param obj - the value of the new entry in the locator table.
	 */
	public void put(ObjectMetadata metadata, UniqueObject obj);

	/**
	 * Returns the UniqueObject corresponding to the metadata.
	 * 
	 * @param metadata - the key to find in the locator table.
	 * @return the corresponding UniqueObject.
	 */
	public UniqueObject get(ObjectMetadata metadata);
}
