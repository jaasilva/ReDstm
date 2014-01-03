package org.deuce.distribution.replication.partial.protocol.score;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.replication.group.Group;
import org.deuce.distribution.replication.partial.PartialReplicationOID;
import org.deuce.distribution.replication.partial.protocol.score.msgs.ReadDone;
import org.deuce.profiling.Profiler;
import org.deuce.transaction.score.SCOReContext;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 */
@ExcludeTM
public class SCOReProtocol_noReadOpt extends SCOReProtocol
{
	@Override
	protected ReadDone processRead(SCOReContext sctx, TxField field)
	{
		ObjectMetadata meta = field.getMetadata();
		boolean firstRead = !sctx.firstReadDone;
		Group p_group = ((PartialReplicationOID) meta).getPartialGroup();
		ReadDone read = null;

		// If I belong to the local group, then I replicate this object
		boolean localObj = p_group.isLocal();

		if (localObj)
		{ // Do *LOCAL* read
			Profiler.onTxLocalReadBegin(sctx.threadID);
			read = sctx.doReadLocal((VBoxField) field);
			Profiler.onTxLocalReadFinish(sctx.threadID);
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
