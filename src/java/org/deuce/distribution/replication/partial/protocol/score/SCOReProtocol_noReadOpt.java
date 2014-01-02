package org.deuce.distribution.replication.partial.protocol.score;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.PartialReplicationOID;
import org.deuce.distribution.replication.partial.protocol.score.msgs.ReadDone;
import org.deuce.profiling.Profiler;
import org.deuce.transaction.score.SCOReContext;
import org.deuce.transform.ExcludeTM;

/**
 * @author jaasilva
 */
@ExcludeTM
public class SCOReProtocol_noReadOpt extends SCOReProtocol
{
	@Override
	protected ReadDone processRead(SCOReContext sctx, ObjectMetadata meta,
			boolean firstRead)
	{
		Group p_group = ((PartialReplicationOID) meta).getPartialGroup();
		ReadDone read = null;

		// If I belong to the local group, then I replicate this object
		boolean localObj = p_group.isLocal();

		if (localObj)
		{ // Do *LOCAL* read
			// try
			// {
			Profiler.onTxLocalReadBegin(sctx.threadID);
			read = doRead(sctx.sid, meta);
			Profiler.onTxLocalReadFinish(sctx.threadID);
			// }
			// catch (NullPointerException e)
			// { // XXX check this. this should not happen!!!
			// LOGGER.debug("% Null pointer while reading locally: "
			// + localObj + " " + localGraph + "\n" + meta + "\n"
			// + TribuDSTM.getObject(meta));
			// read = remoteRead(sctx, meta, firstRead, p_group);
			// }
		}
		else
		{ // Do *REMOTE* read
			Profiler.onTxRemoteReadBegin(sctx.threadID);
			read = remoteRead(sctx, meta, firstRead, p_group);
			Profiler.onTxRemoteReadFinish(sctx.threadID);
		}

		return read;
	}
}
