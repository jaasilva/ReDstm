package org.deuce.distribution.replication.partial.protocol.score;

import org.apache.log4j.Logger;
import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.cache.CacheContainer;
import org.deuce.distribution.cache.CacheMsg;
import org.deuce.distribution.cache.iSetMsg;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.msgs.ControlMessage;
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
			read = getValidVersion(sctx.sid, meta, firstRead);

			if (read == null)
			{ // if not in cache. do remote read
				Profiler.onTxRemoteReadBegin(sctx.threadID);
				read = remoteRead(sctx, meta, firstRead, p_group);
				Profiler.onTxRemoteReadFinish(sctx.threadID);
			}
		}

		return read;
	}

	private ReadDone getValidVersion(int sid, ObjectMetadata metadata,
			boolean firstRead)
	{
		Profiler.onCacheTry();
		if (TribuDSTM.cacheContains(metadata))
		{
			CacheContainer v = TribuDSTM.cacheGet(metadata, sid, firstRead);
			if (v != null)
			{
				int validity = v.validity.validity;
				if (sid <= validity)
				{
					Profiler.onCacheHit();
					return new ReadDone(v.value, firstRead ? validity
							: v.version, true);
				}
				else
				{
					Profiler.onCacheNoValidVersion();
				}
			}
			else
			{
				Profiler.onCacheNoVisibleVersion();
			}
		}
		else
		{
			Profiler.onCacheNoKey();
		}
		return null;
	}

	@Override
	protected void processControlMessage(ControlMessage obj)
	{
		TribuDSTM.cacheInvalidateKeys((iSetMsg) obj);
	}

	private void updateCache(ObjectMetadata metadata, ReadDone read)
	{ // put received version in cache
		CacheMsg msg = ((CacheMsg) read.piggyback);
		CacheContainer v = new CacheContainer();
		v.value = read.value;
		v.version = msg.version;
		TribuDSTM.cachePut(metadata, v, msg.validity, msg.groupId,
				read.mostRecent);
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
		TribuDSTM.sendToGroup(payload, p_group); // XXX id is -2

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

		updateCache(metadata, sctx.response);
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

		// piggyback the info of the version to be cached
		CacheMsg msg = new CacheMsg();
		msg.validity = ver.validity == -1 ? lastCommitted : ver.validity - 1;
		msg.version = ver.version;
		msg.groupId = TribuDSTM.getLocalGroup().getId();
		read.piggyback = msg;

		return read;
	}

	@Override
	public void onTxFinished(DistributedContext ctx, boolean committed)
	{
		SCOReContext sctx = (SCOReContext) ctx;

		if (committed)
		{
			TribuDSTM.cacheSetCommittedKeys(sctx.getCommittedKeys());
		}

		LOGGER.debug("FINISH " + sctx.threadID + ":" + sctx.atomicBlockId + ":"
				+ sctx.trxID.split("-")[0] + "= " + committed);
	}
}
