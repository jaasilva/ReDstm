package org.deuce.transaction.score;

import java.io.Serializable;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 *
 */
@ExcludeTM
public class SCOReField extends TxField implements Serializable
{
	@ExcludeTM
	static public enum Type {
		BYTE, BOOLEAN, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, OBJECT
	}
	
	private Version version;
	
	/**
	 * 
	 */
	public SCOReField()
	{
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param ref
	 * @param address
	 */
	public SCOReField(Object ref, long address)
	{
		super(ref, address);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param containersRef
	 * @param idx
	 * @param backend
	 */
	public SCOReField(Object[] containersRef, int idx, Object[] backend)
	{
		super(containersRef, idx, backend);
		// TODO Auto-generated constructor stub
	}

}
