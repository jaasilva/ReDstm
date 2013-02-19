package org.deuce.distribution.replication.full.protocol.voting;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.groupcomm.Address;
import org.deuce.distribution.groupcomm.subscriber.DeliverySubscriber;
import org.deuce.distribution.replication.full.FullReplicationProtocol;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transform.ExcludeTM;


@ExcludeTM
public class Voting extends FullReplicationProtocol implements
		DeliverySubscriber {
	public static final Logger log = Logger.getLogger(Voting.class);
	static {
		try {
			log.removeAllAppenders();
			log.addAppender(new FileAppender(new PatternLayout("%c{1} - %m%n"), System.getProperty("tribu.groupcommunication.group", "tvale")+"id"+Integer.getInteger("tribu.site")+".log", false));
			log.setLevel(Level.TRACE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private final Map<Integer, DistributedContext> contexts = Collections
			.synchronizedMap(new HashMap<Integer, DistributedContext>());

	private final List<PendingTx> pendingTxs = new LinkedList<PendingTx>();
	private final List<PendingResult> pendingResults = new LinkedList<PendingResult>();

	public void init() {
		TribuDSTM.subscribeDeliveries(this);
	}

	public void onDelivery(Object obj, Address src, int payloadSize) {
		if (obj instanceof DistributedContextState) {
			DistributedContextState ctxState = (DistributedContextState) obj;
			PendingTx tx = new PendingTx(src, ctxState);

//			if (src.isLocal()) {
//				Profiler prof = contexts.get(ctxState.ctxID).profiler;
//				prof.onTODelivery();
//				prof.newMsgRecv(payloadSize);
//			}

			// Check for existing result
			PendingResult pendingResult = null;
			boolean exists = false;
			for (PendingResult res : pendingResults) {
				if (res.src.equals(src) && res.msg.ctxID == ctxState.ctxID) {
					exists = true;
					pendingResult = res;
					break;
				}
			}
			// If it exists, set the tx state
			if (exists) {
				tx.result = pendingResult.msg.result ? PendingTx.COMMITTED
						: PendingTx.ABORTED;
				pendingResults.remove(pendingResult);
			}
			pendingTxs.add(tx);
		} else if (obj instanceof ResultMessage) {
			ResultMessage msg = (ResultMessage) obj;

//			if (src.isLocal()) {
//				Profiler prof = contexts.get(msg.ctxID).profiler;
//				prof.onURBDelivery();
//				prof.newMsgRecv(payloadSize);
//			}

			// Check for existing tx
			PendingTx pendingTx = null;
			boolean exists = false;
			for (PendingTx tx : pendingTxs) {
				if (tx.src.equals(src) && tx.ctxState.ctxID == msg.ctxID) {
					exists = true;
					pendingTx = tx;
					break;
				}
			}
			// If it exists, set the tx state
			if (exists) {
				pendingTx.result = msg.result ? PendingTx.COMMITTED
						: PendingTx.ABORTED;
			} else {
				pendingResults.add(new PendingResult(src, msg));
			}
		}
		processTx();
	}

	private void processTx() {
		boolean keepProcessing = true;
		while (keepProcessing) {
			keepProcessing = false;
			if (pendingTxs.isEmpty())
				return;

			PendingTx tx = pendingTxs.get(0);
			if (!tx.src.isLocal() && tx.result == PendingTx.WAITING)
				return;

			DistributedContext ctx = null;
			// If tx's result has been received, apply it
			// If not, and tx is local, validate and bcast result
			if (tx.result > PendingTx.VALIDATED) {
				pendingTxs.remove(0);

				if (tx.src.isLocal()) {
					ctx = contexts.get(tx.ctxState.ctxID);
				} else {
					ctx = (DistributedContext) ContextDelegator.getInstance();
					ctx.recreateContextFromState(tx.ctxState);
				}

				if (tx.result == PendingTx.COMMITTED) {
					ctx.applyWriteSet();
					ctx.processed(true);
					
					if (log.isTraceEnabled())
						log.trace(tx.src+":"+tx.ctxState.ctxID+":"+tx.ctxState.atomicBlockId+" committed.");
				} else {
					ctx.processed(false);
					
					if (log.isTraceEnabled())
						log.trace(tx.src+":"+tx.ctxState.ctxID+":"+tx.ctxState.atomicBlockId+" aborted.");
				}
				keepProcessing = true;
			} else if (tx.src.isLocal() && tx.result == PendingTx.WAITING) {
				ctx = contexts.get(tx.ctxState.ctxID);
				boolean valid = ctx.validate();
				tx.result = PendingTx.VALIDATED;
				byte[] payload = ObjectSerializer
						.object2ByteArray(new ResultMessage(tx.ctxState.ctxID,
								valid));

//				ctx.profiler.onURBSend();
//				ctx.profiler.newMsgSent(payload.length);

				TribuDSTM.sendReliably(payload);
			}
		}
	}

	public void onTxBegin(DistributedContext ctx) {
	}

	public void onTxCommit(DistributedContext ctx) {
		DistributedContextState ctxState = ctx.createState();
		ctxState.rs = null;
		byte[] payload = ObjectSerializer.object2ByteArray(ctxState);

//		ctx.profiler.onTOSend();
//		ctx.profiler.newMsgSent(payload.length);

		TribuDSTM.sendTotalOrdered(payload);
	}

	public void onTxFinished(DistributedContext ctx, boolean committed) {
	}

	public void onTxContextCreation(DistributedContext ctx) {
		contexts.put(ctx.threadID, ctx);
	}
}