package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * Represents a base class for field write access.
 * 
 * @author Guy Korland, Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
abstract public class WriteFieldAccess extends ReadFieldAccess
{
	/**
	 * Commits the value in memory.
	 */
	abstract public void put(int txNumber);
}
