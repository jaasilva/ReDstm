package org.deuce.distribution.cache;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.deuce.Defaults;
import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.PartialReplicationOID;
import org.deuce.distribution.replication.partial.PartialReplicationProtocol;
import org.deuce.transaction.score.SCOReContext;
import org.deuce.transaction.score.field.SCOReWriteFieldAccess;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class Cache
{
	private static final Logger LOGGER = Logger.getLogger(Cache.class);
	private static final Map<ObjectMetadata, SortedSet<CacheContainer>> cache = new ConcurrentHashMap<ObjectMetadata, SortedSet<CacheContainer>>(
			50000);
	private static final Map<Integer, Validity> mostRecentValidities = new ConcurrentHashMap<Integer, Validity>(
			TribuDSTM.getNumGroups());
	private static final Set<SCOReWriteFieldAccess> committedKeys = Collections
			.newSetFromMap(new ConcurrentHashMap<SCOReWriteFieldAccess, Boolean>(
					1000));
	private static int lastSentSid = 0;
	public static Invalidation invalidationStrategy;

	private static final ScheduledExecutorService senderExec = Executors
			.newSingleThreadScheduledExecutor();
	public static final Executor recvExec = Executors
			.newFixedThreadPool(TribuDSTM.getNumGroups());

	public static AtomicInteger a = new AtomicInteger(0);
	public static AtomicInteger b = new AtomicInteger(0);
	public static AtomicInteger z = new AtomicInteger(0);

	@ExcludeTM
	public enum Invalidation
	{
		EAGER, LAZY, BATCH;
	}

	public Cache(String invalidation)
	{
		if (invalidation.equals("eager"))
		{
			invalidationStrategy = Invalidation.EAGER;
		}
		else if (invalidation.equals("lazy"))
		{
			invalidationStrategy = Invalidation.LAZY;
		}
		else if (invalidation.equals("batch"))
		{
			invalidationStrategy = Invalidation.BATCH;

			if (TribuDSTM.isGroupMaster())
			{
				int interval = Integer.getInteger(
						Defaults._CACHE_BATCH_INTERVAL,
						Defaults.CACHE_BATCH_INTERVAL);

				senderExec.scheduleAtFixedRate(new InvalidationSenderHandler(),
						5, interval, TimeUnit.MILLISECONDS);
			}
		}
		else
		{
			System.err
					.println("Unknown invalidation strategy: " + invalidation);
			System.exit(-1);
		}
	}

	public synchronized void put(ObjectMetadata metadata,
			CacheContainer newVer, int validity, int group, boolean mostRecent)
	{
		SortedSet<CacheContainer> vers = null;

		if (cache.containsKey(metadata))
		{ // some version(s) already inserted
			vers = cache.get(metadata);
			CacheContainer first = vers.first(); // most recent version
			if (newVer.version > first.version)
			{ // installing newer version
				Validity mrv = mostRecentValidities.get(group);

				if (mrv != null)
				{
					if (first.validity.isShared)
					{ // unlink old version's validity
						first.validity = new Validity(first.validity.validity,
								false);
					}
					if (validity == mrv.validity)
					{ // link new version's validity
						newVer.validity = mrv;
						z.incrementAndGet();
					}
				}
				if (newVer.validity == null)
				{
					newVer.validity = new Validity(validity, false);
				}
				a.incrementAndGet();
			}
			else if (newVer.version == first.version)
			{ // updating validity
				if (!first.validity.isShared)
				{
					if (validity > first.validity.validity)
					{ // update version's validity
						first.validity.validity = validity;
					}
				}
				b.incrementAndGet();
			}
			else
			{ // installing older version
				newVer.validity = new Validity(validity, false);
			}
		}
		else
		{ // first version to be inserted for this key
			vers = Collections
					.synchronizedSortedSet(new TreeSet<CacheContainer>());
			cache.put(metadata, vers);

			Validity mrv = mostRecentValidities.get(group);
			if (mrv != null)
			{
				if (validity == mrv.validity)
				{ // link new version's validity
					newVer.validity = mrv;
					z.incrementAndGet();
				}

			}
			if (newVer.validity == null)
			{
				newVer.validity = new Validity(validity, false);
			}
		}

		vers.add(newVer);
	}

	public synchronized boolean contains(ObjectMetadata metadata)
	{
		return cache.containsKey(metadata);
	}

	public synchronized CacheContainer getVisibleVersion(
			ObjectMetadata metadata, int sid, boolean firstRead)
	{ // assumes that the key exists in the map
		SortedSet<CacheContainer> vers = cache.get(metadata);

		if (firstRead)
		{
			return vers.first();
		}

		Iterator<CacheContainer> it = vers.iterator();
		CacheContainer v = null;
		while (it.hasNext())
		{
			v = it.next();
			if (v.version <= sid)
			{
				return v;
			}
		}

		return null;
	}

	/****************************************************
	 * INVALIDATION
	 ***************************************************/

	public synchronized void addCommittedKeys(Set<SCOReWriteFieldAccess> set)
	{
		committedKeys.addAll(set);

		if (invalidationStrategy == Invalidation.EAGER
				&& TribuDSTM.isGroupMaster())
		{
			senderExec.execute(new InvalidationSenderHandler());
		}
	}

	public synchronized iSetMsg getInvalidationSet()
	{
		iSetMsg msg = null;
		int mostRecentSid = SCOReContext.commitId.get();

		if (mostRecentSid > lastSentSid /* && !committedKeys.isEmpty() */)
		{
			Set<SCOReWriteFieldAccess> keys = new HashSet<SCOReWriteFieldAccess>(
					committedKeys);
			committedKeys.clear();

			Set<ObjectMetadata> iSet = new HashSet<ObjectMetadata>(keys.size());
			for (SCOReWriteFieldAccess m : keys)
			{
				PartialReplicationOID meta = (PartialReplicationOID) m
						.getDistMetadata();
				Group pg = meta.getPartialGroup();
				if (!pg.isAll() && pg.isLocal())
				{
					iSet.add(meta);
				}
			}

			msg = new iSetMsg(iSet, mostRecentSid, TribuDSTM.getLocalGroup()
					.getId());

			lastSentSid = mostRecentSid;
		}

		return msg;
	}

	@ExcludeTM
	class InvalidationSenderHandler implements Runnable
	{
		@Override
		public void run()
		{
			iSetMsg msg = getInvalidationSet();

			if (msg != null)
			{
				LOGGER.trace("% Send iSet (" + msg.iSet.size() + ")");

				byte[] payload = ObjectSerializer.object2ByteArray(msg);
				PartialReplicationProtocol.serializationReadCtx.set(true);
				TribuDSTM.sendReliably(payload); // XXX send to other groups
				PartialReplicationProtocol.serializationReadCtx.set(false);
			}
		}
	}

	public void processInvalidationMessage(iSetMsg msg)
	{
		if (msg.group != TribuDSTM.getLocalGroup().getId())
		{ // if is not my group
			recvExec.execute(new InvalidationReceiverHandler(msg));
		}
	}

	@ExcludeTM
	class InvalidationReceiverHandler implements Runnable
	{
		private iSetMsg msg;

		public InvalidationReceiverHandler(iSetMsg msg)
		{
			this.msg = msg;
		}

		@Override
		public void run()
		{
			invalidateKeys(msg.group, msg.mostRecentSid, msg.iSet);
		}
	}

	public synchronized void invalidateKeys(int group, int mostRecent,
			Collection<ObjectMetadata> iSet)
	{
		LOGGER.trace("$ Received iSet (" + iSet.size() + ") from group: "
				+ group);

		for (ObjectMetadata k : iSet)
		{
			SortedSet<CacheContainer> vers = cache.get(k);
			if (vers != null)
			{
				CacheContainer v = vers.first(); // most recent version
				if (v.validity.isShared)
				{
					v.validity = new Validity(v.validity.validity, false);
				}
			}
		}

		Validity mostRecentValidity = mostRecentValidities.get(group);
		if (mostRecentValidity == null)
		{
			Validity validity = new Validity(mostRecent, true);
			mostRecentValidities.put(group, validity);
		}
		else
		{
			mostRecentValidity.validity = mostRecent;
		}
	}
}
