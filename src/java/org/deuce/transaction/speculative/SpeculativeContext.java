package org.deuce.transaction.speculative;

import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.TransactionException;

public abstract class SpeculativeContext extends DistributedContext
{
	final static public TransactionException EARLY_CONFLICT = new TransactionException(
			"Latest speculative version is fresher than this tx.");

	protected boolean aborted = false;

	public SpeculativeContext()
	{
		super();
	}

	/**
	 * Validates the DCtx against the current speculative state of the TM.
	 * 
	 * @return if the transaction was successfully validated
	 */
	final public boolean speculativeValidate()
	{
		// profiler.onTxSpecValidateBegin();

		boolean valid = performSpeculativeValidation();

		// profiler.onTxSpecValidateEnd();

		return valid;
	}

	/**
	 * Template method that performs the speculative validation of the concrete
	 * instance.
	 */
	abstract protected boolean performSpeculativeValidation();

	/**
	 * Speculatively applies the write set to the TM. Must only be called if the
	 * transaction was successfully {speculatively,} validated first.
	 */
	final public void speculativeApplyWriteSet()
	{
		// profiler.onTxSpecCommitStart();

		applySpeculativeUpdates();

		// profiler.txSpecCommitted();
		// profiler.onTxSpecCommitEnd();
	}

	/**
	 * Template method that speculatively applies the write set updates in the
	 * concrete instance.
	 */
	abstract protected void applySpeculativeUpdates();

	final public void abort()
	{
		aborted = true;
	}

	final public boolean isAborted()
	{
		return aborted;
	}

	final public void speculativeAbort()
	{
		// profiler.onTxSpecAbortStart();

		performSpeculativeAbort();

		// profiler.txSpecAborted();
		// profiler.onTxSpecAbortEnd();
	}

	/**
	 * Template method that rollbacks the speculative commit in the concrete
	 * instance.
	 */
	abstract protected void performSpeculativeAbort();

	abstract public int getSpeculativeVersionNumber();

	abstract public void resetSpeculativeVersionNumbers();
}
