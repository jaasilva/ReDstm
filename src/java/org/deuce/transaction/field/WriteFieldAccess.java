package org.deuce.transaction.field;

import org.deuce.transform.ExcludeTM;

/**
 * Represents a base class for field write access.
 * 
 * @author Guy Korland
 */
@ExcludeTM
abstract public class WriteFieldAccess extends ReadFieldAccess
{
	private static final long serialVersionUID = 1L;

	/**
	 * Commits the value in memory.
	 */
	abstract public void put();
}
