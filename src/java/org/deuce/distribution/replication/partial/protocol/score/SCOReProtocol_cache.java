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
	protected ReadDone processRead(SCOReContext sctx, TxField field)
	{
		ObjectMetadata meta = field.getMetadata();
		boolean firstRead = !sctx.firstReadDone;
		Group p_group = ((PartialReplicationOID) meta).getPartialGroup();
		Group group = ((PartialReplicationOID) meta).getGroup();
		ReadDone read = null;

		// If I belong to the local group, then I replicate this object
		boolean localObj = p_group.isLocal();
		/*
		 * if the groups are equal I have the graph from the partial txField
		 * down cached in the locator table
		 */
		boolean localGraph = p_group.equals(group);

		if (localObj || localGraph)
		{ // Do *LOCAL* read
			Profiler.onTxLocalReadBegin(sctx.threadID);
			read = sctx.doReadLocal((VBoxField) field);
			Profiler.onTxLocalReadFinish(sctx.threadID);
		}
		else
		{ // Do *REMOTE* read
			Profiler.onCacheTry(); // first check cache
			if (TribuDSTM.cacheContains(meta))
			{
				Profiler.onCacheHit();
				read = checkCache(sctx.sid, meta);
			}
			else
			{ // if not in cache. do remote read
				Profiler.onTxRemoteReadBegin(sctx.threadID);
				read = remoteRead(sctx, meta, firstRead, p_group);
				Profiler.onTxRemoteReadFinish(sctx.threadID);
			}
		}

		return read;
	}

	private ReadDone checkCache(int sid, ObjectMetadata metadata)
	{ // XXX checkCache
		int origNextId;
		do
		{
			origNextId = SCOReContext.nextId.get();
		} while (!SCOReContext.nextId.compareAndSet(origNextId,
				Math.max(origNextId, sid)));

		// use versions list from cache
		Version lastVersion = TribuDSTM.cacheGet(metadata);

		Version ver = lastVersion.get(sid);
		boolean mostRecent = ver.equals(lastVersion);
		int lastCommitted = SCOReContext.commitId.get();
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
		// put received version list in cache
		System.out.println("put "+metadata);
		TribuDSTM.cachePut(metadata, (Version) sctx.response.piggyback);

		return sctx.response;
	}

	@Override
	protected ReadDone doReadRemote(int sid, ObjectMetadata metadata)
	{
		int origNextId;
		do
		{
			origNextId = SCOReContext.nextId.get();
		} while (!SCOReContext.nextId.compareAndSet(origNextId,
				Math.max(origNextId, sid)));

		VBoxField field = (VBoxField) TribuDSTM.getObject(metadata);

		long st = System.nanoTime();
		while (SCOReContext.commitId.get() < sid
				&& !((InPlaceRWLock) field).isExclusiveUnlocked())
		{ /*
		 * wait until (commitId.get() >= sid || ((InPlaceRWLock)
		 * field).isExclusiveUnlocked())
		 */
		}
		long end = System.nanoTime();
		Profiler.onWaitingRead(end - st);

		Version ver = field.getLastVersion().get(sid);
		boolean mostRecent = ver.equals(field.getLastVersion());
		int lastCommitted = SCOReContext.commitId.get();
		ReadDone read = new ReadDone(ver.value, lastCommitted, mostRecent);
		read.piggyback = field.getLastVersion(); // assign versions list
		return read;
	}
}
