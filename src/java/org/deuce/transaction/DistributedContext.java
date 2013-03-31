package org.deuce.transaction;

import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

import org.deuce.distribution.TribuDSTM;
import org.deuce.transform.ExcludeTM;

/**
 * Distributed Context.
 * 
 * @author Tiago Vale
 */
@ExcludeTM
public abstract class DistributedContext implements ContextMetadata
{
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
	private Semaphore trxProcessed;
	/**
	 * Result of the distributed commit.
	 */
	private boolean committed;
	/**
	 * The transaction's read set.
	 */
	protected ReadSet readSet;
	/**
	 * The transaction's write set.
	 */
	protected WriteSet writeSet;

	public DistributedContext()
	{
		readSet = createReadSet();
		writeSet = createWriteSet();
		threadID = threadIDCounter.getAndIncrement();
		committed = false;
		trxProcessed = new Semaphore(0);

		TribuDSTM.onContextCreation(this);
	}

	/**
	 * Constructs a local DCtx instance based on a description of the state of a
	 * remote one.
	 */
	public void recreateContextFromState(DistributedContextState ctxState)
	{
		readSet = ctxState.rs;
		writeSet = ctxState.ws;
	}

	/**
	 * Factory method that delegates the concrete read set instance creation.
	 * 
	 * @return the concrete read set instance
	 */
	abstract protected ReadSet createReadSet();

	/**
	 * Factory method that delegates the concrete write set instance creation.
	 * 
	 * @return the concrete write set instance
	 */
	abstract protected WriteSet createWriteSet();

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
		readSet.clear();
		writeSet.clear();

		initialise(atomicBlockId, metainf);
		TribuDSTM.onTxBegin(this);
	}

	/**
	 * Template method that performs the state reset on the concrete instance to
	 * begin a new transaction.
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
		boolean valid = performValidation();
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
		applyUpdates();
	}

	/**
	 * Template method that applies the write set updates in the concrete
	 * instance.
	 */
	abstract protected void applyUpdates();

	/**
	 * Triggers the distributed commit, and waits until it is processed.
	 */
	public boolean commit()
	{
		if (writeSet.isEmpty())
		{ // read-only transaction // XXX isto é sempre assim?
			TribuDSTM.onTxFinished(this, true);
			return true;
		}

		TribuDSTM.onTxCommit(this);
		try
		{ // blocked awaiting distributed validation
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

	/**
	 * Notifies that the distributed commit has finished processing.
	 * 
	 * @param committed
	 *            if the distributed commit was sucessful
	 */
	public void processed(boolean committed)
	{
		this.committed = committed;
		trxProcessed.release(); // distributed validation finished
	}

	public void rollback()
	{
		TribuDSTM.onTxFinished(this, false);
	}
}
