package org.deuce.distribution.replication.partial.protocol.score;

import org.apache.log4j.Logger;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.PartialReplicationOID;
import org.deuce.distribution.replication.partial.protocol.score.msgs.ReadDone;
import org.deuce.profiling.Profiler;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.score.SCOReContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 */
@ExcludeTM
public class SCOReProtocol_noReadOpt extends SCOReProtocol
{
	private static final Logger LOGGER = Logger
			.getLogger(SCOReProtocol_noReadOpt.class);

	@Override
	public Object onTxRead(DistributedContext ctx, TxField field)
	{ // I am the coordinator of this read.
		Profiler.onTxCompleteReadBegin(ctx.threadID);
		ObjectMetadata metadata = field.getMetadata();
		SCOReContext sctx = (SCOReContext) ctx;

		boolean firstRead = !sctx.firstReadDone;
		Group p_group = ((PartialReplicationOID) metadata).getPartialGroup();

		if (firstRead)
		{ // first read of this transaction
			sctx.sid = commitId.get();
			sctx.firstReadDone = true;
		}

		ReadDone read = null;
		boolean local_read = p_group.contains(TribuDSTM.getLocalAddress());
		if (local_read)
		{ // *LOCAL* read
			try
			{
				Profiler.onTxLocalReadBegin(ctx.threadID);
				read = doRead(sctx.sid, metadata);
				Profiler.onTxLocalReadFinish(ctx.threadID);
			}
			catch (NullPointerException e)
			{ // XXX check this. this should not happen!!!
				LOGGER.debug("% Null pointer while reading locally: "
						+ local_read + "\n" + metadata + "\n"
						+ TribuDSTM.getObject(metadata));
				read = remoteRead(sctx, metadata, firstRead, p_group);
			}
		}
		else
		{ // *REMOTE* read
			read = remoteRead(sctx, metadata, firstRead, p_group);
		}

		if (firstRead && read.mostRecent)
		{ // advance our snapshot id to a fresher one
			sctx.sid = Math.max(sctx.sid, read.lastCommitted);
		}

		if (sctx.isUpdate() && !read.mostRecent)
		{ // optimization: abort trx forced to see overwritten data
			LOGGER.debug("Forced to see overwritten data.\nAbort trx "
					+ sctx.trxID.split("-")[0]);
			throw OVERWRITTEN_VERSION_EXCEPTION; // abort transaction
		}
		// added to read set in onReadAccess context method
		Profiler.onTxCompleteReadFinish(ctx.threadID);
		return read.value;
	}
}
