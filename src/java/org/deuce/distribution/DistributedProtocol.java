package org.deuce.distribution;

import org.deuce.distribution.replication.Bootstrap;
import org.deuce.objectweb.asm.Type;
import org.deuce.transaction.DistributedContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * This interface represents the distributed protocol for transactions'
 * validation. Every protocol must implement this interface. This callbacks are
 * called by the corresponding transaction context when certain events occur.
 * 
 * @author tvale
 */
@ExcludeTM
public interface DistributedProtocol
{
	public static final String BOOTSTRAP_DESC = Type
			.getDescriptor(Bootstrap.class);
	public static final String BOOTSTRAP_ID_PARAM_NAME = "id";

	/**
	 * Initializes the protocol in the system bootstrap phase.
	 */
	public void init();

	/**
	 * Is called when a transaction context is created.
	 * 
	 * @param ctx - the created transaction context.
	 */
	public void onTxContextCreation(DistributedContext ctx);

	/**
	 * Is called when a transaction begins.
	 * 
	 * @param ctx - the transaction context of the corresponding transaction.
	 */
	public void onTxBegin(DistributedContext ctx);

	/**
	 * Is called when a transaction starts its (distributed) commit phase.
	 * 
	 * @param ctx - the transaction context of the corresponding transaction.
	 */
	public void onTxCommit(DistributedContext ctx);

	/**
	 * Is called when a transaction is completely finished. Either successfully
	 * or not.
	 * 
	 * @param ctx - the transaction context of the corresponding transaction.
	 * @param committed - true if the transaction finished successfully, false
	 *            otherwise.
	 */
	public void onTxFinished(DistributedContext ctx, boolean committed);

	/**
	 * Is called when a transaction executes a read operation.
	 * 
	 * @param ctx - the transaction context of the corresponding transaction.
	 * @param field - the TxField associated with the field being read.
	 * @return the object written in that TxField.
	 */
	public Object onTxRead(DistributedContext ctx, TxField field);

	/**
	 * Returns the corresponding object serializer for this protocol.
	 * 
	 * @return the object serializer.
	 */
	public ObjectSerializer getObjectSerializer();
}
