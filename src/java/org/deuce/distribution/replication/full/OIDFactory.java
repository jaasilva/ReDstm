package org.deuce.distribution.replication.full;

import org.deuce.transform.ExcludeTM;

/**
 * API of an object identifier (OID) factory. Encapsulates the creation of a
 * specific OID implementation.
 * 
 * @author Tiago Vale
 */
@ExcludeTM
public interface OIDFactory
{
	/**
	 * Generates a fresh OID.
	 * 
	 * @return Freshly generated OID.
	 */
	public OID generateOID();

	/**
	 * Generates a deterministic OID.
	 * 
	 * @param oid
	 *            The desired OID.
	 * @return A deterministically generated OID.
	 */
	/*
	 * TODOs OIDs are not necessarily ints. The parameter of this method should
	 * be a sort of seed that always generates the same OID.
	 */
	public OID generateOID(int oid);
}
