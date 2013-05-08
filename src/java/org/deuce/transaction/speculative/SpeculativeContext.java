package org.deuce.transaction.speculative;

import org.deuce.distribution.TribuDSTM;
import org.deuce.profiling.Profiler;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.ReadSet;
import org.deuce.transaction.TransactionException;
import org.deuce.transaction.WriteSet;

public abstract class SpeculativeContext extends DistributedContext
{

	/**
	 * The transaction's read set.
	 */
	protected ReadSet readSet;

	/**
	 * The transaction's write set.
	 */
	protected WriteSet writeSet;

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
		profiler.onTxSpecValidateBegin();

		boolean valid = performSpeculativeValidation();

		profiler.onTxSpecValidateEnd();

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
		profiler.onTxSpecCommitStart();

		applySpeculativeUpdates();

		profiler.txSpecCommitted();
		profiler.onTxSpecCommitEnd();
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
		profiler.onTxSpecAbortStart();

		performSpeculativeAbort();

		profiler.txSpecAborted();
		profiler.onTxSpecAbortEnd();
	}

	/**
	 * Template method that rollbacks the speculative commit in the concrete
	 * instance.
	 */
	abstract protected void performSpeculativeAbort();

	abstract public int getSpeculativeVersionNumber();

	abstract public void resetSpeculativeVersionNumbers();

	/**
	 * Triggers the distributed commit, and waits until it is processed.
	 */
	public boolean commit()
	{
		profiler.onTxAppCommit();

		if (writeSet.isEmpty())
		{

			if (Profiler.enabled)
				profiler.txCommitted++;

			TribuDSTM.onTxFinished(this, true);
			return true;
		}

		TribuDSTM.onTxCommit(this);
		try
		{
			trxProcessed.acquire();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		TribuDSTM.onTxFinished(this, committed);
		boolean result = committed;
		committed = false;
		return result;
	}
}
