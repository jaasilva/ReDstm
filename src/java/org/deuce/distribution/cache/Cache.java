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
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
	private static final ConcurrentMap<ObjectMetadata, SortedSet<CacheContainer>> cache = new ConcurrentHashMap<ObjectMetadata, SortedSet<CacheContainer>>(
			50000);
	private static final Map<Integer, Validity> mostRecentValidities = new ConcurrentHashMap<Integer, Validity>(
			TribuDSTM.getNumGroups());
	private static final Set<SCOReWriteFieldAccess> committedKeys = Collections
			.newSetFromMap(new ConcurrentHashMap<SCOReWriteFieldAccess, Boolean>(
					1000));
	private static int lastSentSid = 0;
	public static invalidation invStrategy;

	private static final ScheduledExecutorService exec = Executors
			.newSingleThreadScheduledExecutor();
	public static final Executor exec2 = Executors.newFixedThreadPool(TribuDSTM
			.getNumGroups());

	@ExcludeTM
	public enum invalidation
	{
		EAGER, LAZY, BATCH;
	}

	public Cache()
	{
		String inv = System
				.getProperty(Defaults._CACHE_INV, Defaults.CACHE_INV);

		if (inv.equals("eager"))
		{
			invStrategy = invalidation.EAGER;
			// TODO implement this invalidation strategy
		}
		else if (inv.equals("lazy"))
		{
			invStrategy = invalidation.LAZY;
			// TODO implement this invalidation strategy
		}
		else if (inv.equals("batch"))
		{
			invStrategy = invalidation.BATCH;

			if (TribuDSTM.isGroupMaster())
			{
				exec.scheduleAtFixedRate(new InvalidationSenderHandler(), 50,
						50, TimeUnit.MILLISECONDS);
			}
		}
		else
		{
			System.err.println("Unknown invalidation strategy: " + inv);
			System.exit(-1);
		}
	}

	public synchronized void put(ObjectMetadata metadata, CacheContainer ver,
			int validity, int group, boolean mostRecent)
	{
		SortedSet<CacheContainer> vers = null;
		System.out.println("*Installing " + metadata.toString().split("-")[0]
				+ " ver:" + ver.version + " val:" + validity + " g:" + group);

		if (cache.containsKey(metadata))
		{ // some version(s) already inserted
			vers = cache.get(metadata);
			CacheContainer origVer = vers.first(); // most recent version
			if (ver.version > origVer.version)
			{ // installing newer version
				System.out.println("    installing newer version mrv:"
						+ origVer.version + " " + origVer.validity.validity
						+ " " + origVer.validity.isShared);

				if (origVer.validity.isShared)
				{
					origVer.validity = new Validity(origVer.validity.validity,
							false);
				}

				Validity mrv = mostRecentValidities.get(group);
				if (mrv == null)
				{
					mrv = new Validity(validity, true);
					mostRecentValidities.put(group, mrv);
				}

				if (validity == mrv.validity)
				{
					ver.validity = mrv;
				}
				else
				{
					Validity val = new Validity(validity, false);
					ver.validity = val;
				}

				System.out.println("      " + origVer.validity.isShared + " "
						+ ver.validity.isShared + " " + mrv.validity);
			}
			else if (ver.version == origVer.version)
			{ // updating validity
				System.out.println("    installing equal version mrv:"
						+ origVer.version + " " + origVer.validity.validity
						+ " " + origVer.validity.isShared);

				if (origVer.validity.isShared)
				{
					origVer.validity = new Validity(validity, false);
				}
				else
				{
					origVer.validity.validity = validity;
				}

				System.out.println("      " + origVer.validity.isShared);
			}
			else
			{ // installing older version XXX will this ever happen?
				System.out.println("    installing older version mrv:"
						+ origVer.version + " " + origVer.validity.validity
						+ " " + origVer.validity.isShared);

				Validity val = new Validity(validity, false);
				ver.validity = val;

				System.out.println("$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$");
			}
		}
		else
		{ // first version to be inserted for this key
			System.out.println("    installing first version");

			vers = Collections
					.synchronizedSortedSet(new TreeSet<CacheContainer>());
			cache.put(metadata, vers);

			Validity mrv = mostRecentValidities.get(group);
			if (mrv == null)
			{
				mrv = new Validity(validity, true);
				mostRecentValidities.put(group, mrv);
			}

			if (validity == mrv.validity)
			{
				ver.validity = mrv;
			}
			else
			{
				Validity val = new Validity(validity, false);
				ver.validity = val;
			}

			System.out.println("      " + ver.validity.isShared + " "
					+ mrv.validity);
		}

		vers.add(ver);
	}

	public synchronized boolean contains(ObjectMetadata metadata)
	{
		return cache.containsKey(metadata);
	}

	public synchronized CacheContainer getVisibleVersion(
			ObjectMetadata metadata, int sid, boolean firstRead)
	{
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

	public synchronized void committedKeys(Set<SCOReWriteFieldAccess> set)
	{
		committedKeys.addAll(set);
	}

	public synchronized iSetMsg getInvalidationSet()
	{
		iSetMsg msg = null;
		int mostRecentSid = SCOReContext.commitId.get();

		if (mostRecentSid > lastSentSid && !committedKeys.isEmpty())
		{
			Set<SCOReWriteFieldAccess> keys = new HashSet<SCOReWriteFieldAccess>(
					committedKeys);
			committedKeys.clear();

			Set<ObjectMetadata> iSet = new HashSet<ObjectMetadata>(keys.size());
			for (SCOReWriteFieldAccess m : keys)
			{
				Group pg = ((PartialReplicationOID) m.getDistMetadata())
						.getPartialGroup();
				if (pg.isLocal() && !pg.isAll())
				{
					iSet.add(m.getDistMetadata());
				}
			}

			if (!iSet.isEmpty())
			{
				msg = new iSetMsg(iSet, mostRecentSid, TribuDSTM
						.getLocalGroup().getId());
			}

			lastSentSid = mostRecentSid;

			// System.out.println("################ getInvalidationSet "
			// + iSet.size());
		}

		return msg;
	}

	public synchronized void invalidateKeys(int group, int mostRecent,
			Collection<ObjectMetadata> iSet)
	{
		// System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>> invalidateKeys g:"
		// + group + " mrv:" + mostRecent + " iSet:" + iSet.size());
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
			// System.out.println("    new mrv:" + mostRecent);
			Validity validity = new Validity(mostRecent, true);
			mostRecentValidities.put(group, validity);
		}
		else
		{
			// System.out.println("    update mrv:" + mostRecent);
			mostRecentValidity.validity = mostRecent;
		}
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
				byte[] payload = ObjectSerializer.object2ByteArray(msg);

				PartialReplicationProtocol.serializationReadContext.set(true);
				TribuDSTM.sendReliably(payload); // XXX send to other groups
				PartialReplicationProtocol.serializationReadContext.set(false);
			}
		}
	}

	public void processInvalidationMessage(iSetMsg msg)
	{
		if (msg.group != TribuDSTM.getLocalGroup().getId())
		{
			exec2.execute(new InvalidationReceiverHandler(msg));
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

}
