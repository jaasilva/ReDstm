package org.deuce.transaction.score2.field;

import org.deuce.transaction.score2.InPlaceLock;
import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public interface VBox extends InPlaceLock
{
	@ExcludeTM
	static public enum Type
	{
		BYTE, BOOLEAN, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, OBJECT
	}

	boolean validate(Version version, int owner);

	Version get(int version);

	Version getTop();

	void commit(Object value, int txNumber);
}
