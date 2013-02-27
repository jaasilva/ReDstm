package org.deuce.transaction.field;

import org.deuce.transaction.SpeculativeContext;
import org.deuce.transform.ExcludeTM;

/**
 * Represents a base class for field write access.
 * 
 * @author Guy Korland
 */
@ExcludeTM
abstract public class SpeculativeWriteFieldAccess extends WriteFieldAccess
{
	private static final long serialVersionUID = 1L;

	abstract public void speculativePut(SpeculativeContext ctx);

	abstract public void speculativeRemove();
}
