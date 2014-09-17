package org.deuce.distribution;

import org.deuce.transform.ExcludeTM;

/**
 * This class has all the default properties used by the system. ***PLEASE USE
 * THIS CLASS WHEN REFERING ONE OF THESE PROPERTIES.***
 * 
 * @author jaasilva
 */
@ExcludeTM
public class Defaults
{
	/********************************************************************
	 * Contexts
	 *******************************************************************/

	public static final int MAX_RETRIES = Integer.MAX_VALUE;
	public static final String METAINF = "";
	public static final String _LOCAL_METADATA = "org.deuce.transform.localmetadata.type.TxField";

	public static final String _TL2_TX_LOAD_OPT = "org.deuce.transaction.tl2.txload.opt";

	public static final int SCORE_MAX_VERSIONS = 16;
	public static final String _SCORE_MAX_VERSIONS = "org.deuce.transaction.score.versions";

	public static final int MVSTM_MAX_VERSIONS = 16;
	public static final String _MVSTM_MAX_VERSIONS = "org.deuce.transaction.mvstm.versions";

	/********************************************************************
	 * TribuDSTM Configuration
	 *******************************************************************/

	public static final String SER_COMPRESS = "true";
	public static final String _SER_COMPRESS = "tribu.serialization.compress";
	public static final String COMM_CLASS = "org.deuce.distribution.groupcomm.jgroups.JGroupsGroupCommunication";
	public static final String _COMM_CLASS = "tribu.groupcommunication.class";
	public static final String CTX_CLASS = "org.deuce.transaction.tl2.Context";
	public static final String _CTX_CLASS = "org.deuce.transaction.contextClass";
	public static final String PROTO_CLASS = "org.deuce.distribution.replication.full.protocol.nonvoting.NonVoting";
	public static final String _PROTO_CLASS = "tribu.distributed.protocolClass";
	public static final String COMM_GROUP = "tvale";
	public static final String _COMM_GROUP = "tribu.groupcommunication.group";
	public static final String _REPLICAS = "tribu.replicas";
	public static final String COMM_SPREAD_DAEMON = "localhost";
	public static final String _COMM_SPREAD_DAEMON = "tribu.groupcommunication.spread.daemon";
	public static final String _SITE = "tribu.site";

	/********************************************************************
	 * Configuration Files
	 *******************************************************************/

	public static final String APPIA_CONF_FILE = "etc/appia-tob.xml";
	public static final String JGROUPS_CONF_FILE = "etc/jgroups.xml";

	/********************************************************************
	 * Partial Replication
	 *******************************************************************/

	public static final int GROUPS = 1;
	public static final String _GROUPS = "tribu.groups";
	public static final String PARTIAL_MODE = "false";
	public static final String _PARTIAL_MODE = "tribu.distributed.PartialReplicationMode";
	public static final String GP_CLASS = "org.deuce.distribution.replication.partitioner.group.RoundRobinGroupPartitioner";
	public static final String _GP_CLASS = "tribu.distributed.GroupPartitionerClass";
	public static final String DP_CLASS = "org.deuce.distribution.replication.partitioner.data.RoundRobinDataPartitioner";
	public static final String _DP_CLASS = "tribu.distributed.DataPartitionerClass";

	/********************************************************************
	 * Cache
	 *******************************************************************/

	public static final String _CACHE = "tribu.distributed.partial.cache";
	public static final String CACHE = "false";
	public static final String _CACHE_INV = "tribu.distributed.partial.cache.invalidation";
	public static final String CACHE_INV = "eager"; // eager, lazy or batch
	public static final String _CACHE_BATCH_INTERVAL = "tribu.distributed.partial.cache.invalidation.batch.interval";
	public static final int CACHE_BATCH_INTERVAL = 50; // milliseconds

	/********************************************************************
	 * Benchmarks
	 *******************************************************************/

	public static final String _RBTREE_PARTIAL_OPS = "benchmarks.rbtree.partial_ops";
	public static final int RBTREE_PARTIAL_OPS = 0;
	public static final String _RBTREE_INITIAL = "benchmarks.rbtree.initial";
	public static final int RBTREE_INITIAL = 32768;
	public static final String _RBTREE_RR = "benchmarks.rbtree.remote";
	public static final int RBTREE_RR = 25;

	/********************************************************************
	 * Java Agent
	 *******************************************************************/

	public static final String _AGENT_LOGGER = "org.deuce.agent";
	public static final String AGENT_VERBOSE = "false";
	public static final String _AGENT_VERBOSE = "org.deuce.verbose";
	public static final String AGENT_GLOBAL_TXN = "false";
	public static final String _AGENT_GLOBAL_TXN = "org.deuce.transaction.global";
	public static final String AGENT_POOL = "false";
	public static final String _AGENT_POOL = "org.tribu.pool";
}
