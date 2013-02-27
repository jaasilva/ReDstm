package org.deuce.distribution.replication.full.protocol.nonvoting;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.OptimisticDeliverySubscriber;
import org.deuce.distribution.replication.full.FullReplicationProtocol;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.SpeculativeContext;
import org.deuce.transaction.SpeculativeContextState;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class SpeculativeNonVoting extends FullReplicationProtocol implements
		OptimisticDeliverySubscriber
{

	/*
	 * TODO Ter um conhecimento mais fino: saber quais os que estão a executar
	 * e quais os que estão pendentes. TODO É necessário distinguir entre tx
	 * read-only e restantes no caso
	 */
	private final Map<Integer, SpeculativeContext> contexts = Collections
			.synchronizedMap(new HashMap<Integer, SpeculativeContext>());
	private final Set<SpeculativeContext> waitingContexts = Collections
			.synchronizedSet(new HashSet<SpeculativeContext>());
	private final Set<SpeculativeContext> executingContexts = Collections
			.synchronizedSet(new HashSet<SpeculativeContext>());

	private final Set<SpeculativeContext> specComm = new HashSet<SpeculativeContext>();
	private final Set<SpeculativeContext> specAborted = new HashSet<SpeculativeContext>();

	private final List<SpeculativeContext> optDelivered = new LinkedList<SpeculativeContext>();

	private volatile boolean handlingOutOfOrder = false;
	private final Lock outOfOrderLock = new ReentrantLock();
	private final Condition finishedHandlingOutOfOrder = outOfOrderLock
			.newCondition();
	private final Condition worldStopped = outOfOrderLock.newCondition();

	public void init()
	{
		TribuDSTM.subscribeOptimisticDeliveries(this);
	}

	public void onDelivery(Object obj, Address src, int payloadSize)
	{
		SpeculativeContext ctx = (SpeculativeContext) obj;

		// ctx.profiler.onTODelivery();

		if (ctx.isAborted())
		{
			optDelivered.remove(ctx);
			ctx.processed(false);
		}
		else
		{
			if (optDelivered.get(0) != ctx)
			{
				handleOutOfOrder(ctx);

				// ctx.profiler.txOutOfOrder();
			}
			else
			{
				optDelivered.remove(0);
				if (specAborted.contains(ctx))
				{
					specAborted.remove(ctx);
					ctx.processed(false);
				}
				else
				{
					specComm.remove(ctx);
					try
					{
						ctx.applyWriteSet();
					}
					catch (Exception e)
					{
						System.err
								.println("Couldn't apply write set. This should never happen!");
						e.printStackTrace();
						System.exit(-1);
					}
					ctx.processed(true);
				}
			}
		}
	}

	public Object onOptimisticDelivery(Object obj, Address src, int payloadSize)
	{
		SpeculativeContext ctx = null;
		DistributedContextState ctxState = (SpeculativeContextState) obj;

		if (src.isLocal())
		{
			ctx = contexts.get(ctxState.ctxID);
		}
		else
		{
			ctx = (SpeculativeContext) ContextDelegator.getInstance();
			ctx.recreateContextFromState(ctxState);
		}

		// ctx.profiler.onOptTODelivery();
		// ctx.profiler.newMsgRecv(payloadSize);

		optDelivered.add(ctx);

		if (ctx.validate())
		{
			if (ctx.speculativeValidate())
			{
				specComm.add(ctx);
				try
				{
					ctx.speculativeApplyWriteSet();
				}
				catch (Exception e)
				{
					System.err
							.println("Couldn't speculatively apply write set on optimistic delivery. This should never happen!");
					e.printStackTrace();
					System.exit(-1);
				}
			}
			else
			{
				specAborted.add(ctx);
			}
		}
		else
		{
			ctx.abort();
		}

		return ctx;
	}

	private void handleOutOfOrder(SpeculativeContext ctx)
	{
		optDelivered.remove(ctx);
		boolean outcome = ctx.validate();
		if (!outcome && specAborted.contains(ctx))
		{
			specAborted.remove(ctx);
			ctx.processed(false);
		}
		else
		{
			// temporarily block activation of new transactions
			// abort local transactions not yet in their commit phase
			// System.out.println("handleOutOfOrder: lock");
			outOfOrderLock.lock();
			try
			{
				/*
				 * TODO bloquear transações no onTxBegin aqui e abortar todos
				 * os Contexts em execução (ver topo deste ficheiro) Utilizar
				 * condições em vez do lock?
				 */
				/*
				 * FIXME abortar mesmo, não é só chamar processed(false),
				 * quero mesmo que a transação recomece.
				 */
				// for (Context c : executingContexts)
				// c.processed(false);
				handlingOutOfOrder = true;
				// System.out
				// .println("handleOutOfOrder(): waiting for all executing tx to enter commit phase.");
				while (!executingContexts.isEmpty())
				{
					try
					{
						worldStopped.await();
					}
					catch (InterruptedException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
			finally
			{
				outOfOrderLock.unlock();
			}

			// System.out.println("handleOutOfOrder(): empty? "
			// + executingContexts.isEmpty());

			if (outcome)
			{
				if (specAborted.contains(ctx))
					specAborted.remove(ctx);
				else
					specComm.remove(ctx);
				try
				{
					ctx.applyWriteSet();
				}
				catch (Exception e)
				{
					System.err
							.println("Couldn't apply write set while handling out of order. This should never happen!");
					e.printStackTrace();
					System.exit(-1);
				}
			}
			else
			{
				specComm.remove(ctx);
				ctx.processed(false);
			}
			revalidateSpeculativeTxs();
			handlingOutOfOrder = false;
			outOfOrderLock.lock();
			try
			{
				finishedHandlingOutOfOrder.signalAll();
			}
			finally
			{
				outOfOrderLock.unlock();
				// System.out.println("handleOutOfOrder: unlock");
			}
			// unblock activation of new transactions
		}
	}

	private void revalidateSpeculativeTxs()
	{
		contexts.values().iterator().next().resetSpeculativeVersionNumbers();
		// Remove all speculative versions (version will be reset on
		// speculativeApplyWriteSet()
		for (SpeculativeContext ctx : optDelivered)
		{
			ctx.speculativeAbort();
		}
		for (SpeculativeContext ctx : optDelivered)
		{
			if (ctx.validate())
			{
				if (ctx.speculativeValidate())
				{
					if (specAborted.contains(ctx))
					{
						specAborted.remove(ctx);
						specComm.add(ctx);
					}
					try
					{
						ctx.speculativeApplyWriteSet();
					}
					catch (Exception e)
					{
						System.err
								.println("Couldn't speculatively apply write set while revalidating. This should never happen!");
						e.printStackTrace();
						System.exit(-1);
					}
				}
				else
				{
					if (specComm.contains(ctx))
					{
						specComm.remove(ctx);
						specAborted.add(ctx);
					}
				}
			}
			else
			{
				ctx.processed(false);
			}
		}
	}

	public void onTxBegin(DistributedContext ctx)
	{
		if (handlingOutOfOrder)
		{
			// System.out.println("onTxBegin: lock");
			outOfOrderLock.lock();
			try
			{

				finishedHandlingOutOfOrder.await();
			}
			catch (InterruptedException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally
			{
				outOfOrderLock.unlock();
				// System.out.println("onTxBegin: unlock");
			}
		}

		executingContexts.add((SpeculativeContext) ctx);

	}

	public void onTxCommit(DistributedContext ctx)
	{
		SpeculativeContext specCtx = (SpeculativeContext) ctx;
		executingContexts.remove(specCtx);

		if (handlingOutOfOrder)
		{
			specCtx.processed(false);
			return;
		}

		waitingContexts.add(specCtx);
		if (specCtx.speculativeValidate())
		{
			byte[] payload = ObjectSerializer.object2ByteArray(ctx
					.createState());

			// ctx.profiler.onTOSend();
			// ctx.profiler.newMsgSent(payload.length);

			TribuDSTM.sendTotalOrdered(payload);
		}
		else
		{
			specCtx.processed(false);
		}
	}

	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
		waitingContexts.remove(ctx);
		executingContexts.remove(ctx);
		if (handlingOutOfOrder && executingContexts.isEmpty())
		{
			// System.out.println("onTxFinished: lock");
			outOfOrderLock.lock();
			try
			{
				worldStopped.signal();
			}
			finally
			{
				outOfOrderLock.unlock();
				// System.out.println("onTxFinished: unlock");
			}
		}
	}

	public void onTxContextCreation(DistributedContext ctx)
	{
		contexts.put(ctx.threadID, (SpeculativeContext) ctx);
	}

	@Override
	public Object onTxRead(DistributedContext ctx, ObjectMetadata metadata)
	{
		return null;
		// nothing to do

	}

	@Override
	public void onTxWrite(DistributedContext ctx, ObjectMetadata metadata,
			UniqueObject obj)
	{
		// nothing to do

	}
}
