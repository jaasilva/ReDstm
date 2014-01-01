package org.deuce.distribution.replication.partial.protocol.score;

import org.apache.log4j.Logger;
import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.PartialReplicationOID;
import org.deuce.distribution.replication.partial.protocol.score.msgs.ReadDone;
import org.deuce.distribution.replication.partial.protocol.score.msgs.ReadReq;
import org.deuce.profiling.Profiler;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.score.SCOReContext;
import org.deuce.transaction.score.field.InPlaceRWLock;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transaction.score.field.Version;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 */
@ExcludeTM
public class SCOReProtocol_cache extends SCOReProtocol
{
	private static final Logger LOGGER = Logger
			.getLogger(SCOReProtocol_cache.class);

	@Override
	public Object onTxRead(DistributedContext ctx, TxField field)
	{ // I am the coordinator of this read.
		Profiler.onTxCompleteReadBegin(ctx.threadID);
		ObjectMetadata metadata = field.getMetadata();
		SCOReContext sctx = (SCOReContext) ctx;

		boolean firstRead = !sctx.firstReadDone;
		Group p_group = ((PartialReplicationOID) metadata).getPartialGroup();
		Group group = ((PartialReplicationOID) metadata).getGroup();

		if (firstRead)
		{ // first read of this transaction
			sctx.sid = commitId.get();
			sctx.firstReadDone = true;
		}

		ReadDone read = null;
		boolean local_read = p_group.isLocal();
		boolean local_graph = p_group.equals(group);
		if (local_read || local_graph) // if the groups are equal I have the
		// graph from the partial txField down cached in the locator table
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
						+ local_read + " " + local_graph + "\n" + metadata
						+ "\n" + TribuDSTM.getObject(metadata));
				read = remoteRead(sctx, metadata, firstRead, p_group);
			}
		}
		else
		{ // *REMOTE* read
			Profiler.onCacheTry();
			if (TribuDSTM.cacheContains(metadata))
			{ // XXX cache get
				Profiler.onCacheHit();
				read = checkCache(sctx.sid, metadata);
			}
			else
			{
				read = remoteRead(sctx, metadata, firstRead, p_group);
			}
		}

		if (firstRead && read.mostRecent)
		{ // advance our snapshot id to a fresher one
			sctx.sid = Math.max(sctx.sid, read.lastCommitted);
		}

		if (sctx.isUpdate() && !read.mostRecent)
		{ // optimization: abort trx forced to see overwritten data
			LOGGER.debug("Forced to see overwritten data.\nAbort tx "
					+ sctx.trxID.split("-")[0]);
			throw OVERWRITTEN_VERSION_EXCEPTION; // abort tx
		}
		// added to read set in onReadAccess context method
		Profiler.onTxCompleteReadFinish(ctx.threadID);
		return read.value;
	}

	private ReadDone checkCache(int sid, ObjectMetadata metadata)
	{
		int origNextId;
		do
		{
			origNextId = nextId.get();
		} while (!nextId.compareAndSet(origNextId, Math.max(origNextId, sid)));

		VBoxField field = (VBoxField) TribuDSTM.cacheGet(metadata);

		long st = System.nanoTime();
		while (commitId.get() < sid
				&& !((InPlaceRWLock) field).isExclusiveUnlocked())
		{ // wait until (commitId.get() >= sid || ((InPlaceRWLock)
			// field).isExclusiveUnlocked())
			LOGGER.debug("doRead waiting: " + (commitId.get() < sid) + " "
					+ !((InPlaceRWLock) field).isExclusiveUnlocked());
		}
		long end = System.nanoTime();
		Profiler.onWaitingRead(end - st);

		Version ver = field.getLastVersion().get(sid);
		boolean mostRecent = ver.equals(field.getLastVersion());
		int lastCommitted = commitId.get();
		return new ReadDone(ver.value, lastCommitted, mostRecent);
	}

	@Override
	protected ReadDone remoteRead(SCOReContext sctx, ObjectMetadata metadata,
			boolean firstRead, Group p_group)
	{
		ReadReq req = new ReadReq(sctx.threadID, metadata, sctx.sid, firstRead,
				sctx.requestVersion);

		Profiler.onSerializationBegin(sctx.threadID);
		byte[] payload = ObjectSerializer.object2ByteArray(req);
		Profiler.onSerializationFinish(sctx.threadID);

		Profiler.newMsgSent(payload.length);
		Profiler.onTxRemoteReadBegin(sctx.threadID);

		TribuDSTM.sendToGroup(payload, p_group);

		LOGGER.debug("SEND READ REQ " + sctx.trxID.split("-")[0] + " "
				+ sctx.requestVersion);

		try
		{ // wait for first response
			sctx.syncMsg.acquire();
		}
		catch (InterruptedException e)
		{
			e.printStackTrace();
		}

		TribuDSTM.cachePut(metadata, sctx.response.value); // XXX cache put

		Profiler.onTxRemoteReadFinish(sctx.threadID);
		return sctx.response;
	}
}
