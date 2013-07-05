package org.deuce.transaction;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.distribution.TribuDSTM;
import org.deuce.profiling.PRProfiler;
import org.deuce.transform.ExcludeTM;

/**
 * Distributed Context.
 * 
 * @author Tiago Vale
 */
@ExcludeTM
public abstract class DistributedContext implements ContextMetadata
{
	// final public Profiler profiler;

	/**
	 * There's a 1:1 mapping between DistributedContext (DCtx) instances and
	 * worker threads on a site. This counter acts as an unique identifier
	 * generator for DCtx instances in the same site.
	 */
	final static private AtomicInteger threadIDCounter = new AtomicInteger(0);

	/**
	 * Uniquely identifies a DCtx instance of a specific site.
	 */
	final public int threadID;

	/**
	 * Uniquely identifies the atomic block being executed.
	 */
	public int atomicBlockId;

	/**
	 * Semaphore on which a DCtx waits for the distributed commit to take place.
	 */
	protected Semaphore trxProcessed;

	/**
	 * Result of the distributed commit.
	 */
	protected boolean committed;

	public DistributedContext()
	{
		threadID = threadIDCounter.getAndIncrement();
		committed = false;
		trxProcessed = new Semaphore(0);

		// profiler = new Profiler(false);
		// Profiler.addProfiler(profiler);
		TribuDSTM.onContextCreation(this);
	}

	/**
	 * Constructs a local DCtx instance based on a description of the state of a
	 * remote one.
	 */
	public void recreateContextFromState(DistributedContextState ctxState)
	{
		// profiler.remote = true;
		// Profiler.txRemote++;
	}

	/**
	 * Creates a sort of memento object with this transaction's state, but that
	 * can be manipulated by the distributed commit protocol. For example, in
	 * the Voting Certification scheme, the read set need not be broadcasted.
	 * 
	 * @return the state of the current transaction
	 */
	abstract public DistributedContextState createState();

	/**
	 * Resets the state to begin a new transaction.
	 */
	public void init(int atomicBlockId, String metainf)
	{
		this.atomicBlockId = atomicBlockId;

		initialise(atomicBlockId, metainf);
		TribuDSTM.onTxBegin(this);
		// profiler.txLocal++;
		// profiler.onTxBegin();
		PRProfiler.onTxAppBegin(threadID);
	}

	/**
	 * Template method that performs the state reset on the concrente instance
	 * to begin a new transaction.
	 * 
	 * @param atomicBlockId
	 *            unique identifier of the atomic block describing the
	 *            transaction to be executed (on a per-site basis)
	 * @param metainf
	 *            meta information passed by Deuce
	 */
	abstract protected void initialise(int atomicBlockId, String metainf);

	/**
	 * Validates the DCtx against the current state of the TM.
	 * 
	 * @return if the transaction was successfully validated
	 */
	public boolean validate()
	{
		// profiler.onTxValidateBegin();
		PRProfiler.onTxValidateBegin(threadID);

		boolean valid = performValidation();

		PRProfiler.onTxValidateEnd(threadID);
		// profiler.onTxValidateEnd();

		return valid;
	}

	/**
	 * Template method that performs the validation of the concrete instance.
	 */
	abstract protected boolean performValidation();

	/**
	 * Applies the write set to the TM. Must only be called if the transaction
	 * was successfully validated first.
	 */
	public void applyWriteSet()
	{
		// profiler.onTxCommitStart();
		PRProfiler.onTxCommitBegin(threadID);

		applyUpdates();

		PRProfiler.onTxCommitEnd(threadID);
		// profiler.onTxCommitEnd();
	}

	/**
	 * Template method that applies the write set updates in the concrete
	 * instance.
	 */
	abstract protected void applyUpdates();

	/**
	 * Notifies that the distributed commit has finished processing.
	 * 
	 * @param committed
	 *            if the distributed commit was sucessful
	 */
	public void processed(boolean committed)
	{
		this.committed = committed;

		PRProfiler.txProcessed(committed);
		// if (Profiler.enabled)
		// if (committed)
		// profiler.txCommitted++;
		// else
		// profiler.txAborted++;

		trxProcessed.release();
	}

	public void rollback()
	{
		// if (Profiler.enabled)
		// profiler.txAborted++;
		PRProfiler.txAborted();

		TribuDSTM.onTxFinished(this, false);
	}
}
