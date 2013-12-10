package org.deuce;

/**
 * This class has all the default properties used by the system. PLEASE USE THIS
 * CLASS WHEN REFERING ONE OF THESE PROPERTIES.
 * 
 * @author jaasilva
 */
public class Defaults
{
	public static final int MAX_RETRIES = Integer.MAX_VALUE;
	public static final String METAINF = "";
	public static final String LOCAL_METADATA = "org.deuce.transform.localmetadata.type.TxField";

	public static final String SER_COMPRESS = "true"; // tribu.serialization.compress
	public static final String _SER_COMPRESS = "tribu.serialization.compress";
	public static final String COMM_CLASS = "org.deuce.distribution.groupcomm.jgroups.JGroupsGroupCommunication"; // tribu.groupcommunication.class
	public static final String _COMM_CLASS = "tribu.groupcommunication.class";
	public static final String CTX_CLASS = "org.deuce.transaction.tl2.Context"; // org.deuce.transaction.contextClass
	public static final String _CTX_CLASS = "org.deuce.transaction.contextClass";
	public static final String PROTO_CLASS = "org.deuce.distribution.replication.full.protocol.nonvoting.NonVoting"; // tribu.distributed.protocolClass
	public static final String _PROTO_CLASS = "tribu.distributed.protocolClass";
	public static final String COMM_GROUP = "tvale"; // tribu.groupcommunication.group
	public static final String _COMM_GROUP = "tribu.groupcommunication.group";
	// public static final String REPLICAS = ""; // tribu.replicas
	public static final String _REPLICAS = "tribu.replicas";
	public static final String COMM_SPREAD_DAEMON = "localhost"; // tribu.groupcommunication.spread.daemon
	public static final String _COMM_SPREAD_DAEMON = "tribu.groupcommunication.spread.daemon";
	// public static final String SITE = ""; // tribu.site
	public static final String _SITE = "tribu.site";
	// public static final String EXCLUDE = ""; // org.deuce.exclude
	// public static final String _EXCLUDE = "org.deuce.exclude";
	// public static final String INCLUDE = ""; // org.deuce.include
	// public static final String _INCLUDE = "org.deuce.include";

	public static final int GROUPS = 1; // tribu.groups
	public static final String _GROUPS = "tribu.groups";
	public static final String PARTIAL_MODE = "false"; // tribu.distributed.PartialReplicationMode
	public static final String _PARTIAL_MODE = "tribu.distributed.PartialReplicationMode";
	public static final String GP_CLASS = "org.deuce.distribution.replication.partitioner.group.RoundRobinGroupPartitioner"; // tribu.distributed.GroupPartitionerClass
	public static final String _GP_CLASS = "tribu.distributed.GroupPartitionerClass";
	public static final String DP_CLASS = "org.deuce.distribution.replication.partitioner.data.SimpleDataPartitioner"; // tribu.distributed.DataPartitionerClass
	public static final String _DP_CLASS = "tribu.distributed.DataPartitionerClass";
	public static final String GP_HASH_CLASS = "org.deuce.hashing.MD5GuavaHashFunction"; // tribu.distributed.GroupPartitionerHashClass
	public static final String _GP_HASH_CLASS = "tribu.distributed.GroupPartitionerHashClass";
	public static final int SCORE_MVCC_MAX_VERSIONS = 16; // org.deuce.transaction.score.versions
	public static final String _SCORE_MVCC_MAX_VERSIONS = "org.deuce.transaction.score.versions";
	// public static final String LOG = ""; // log
	// public static final String _LOG = "log";
}