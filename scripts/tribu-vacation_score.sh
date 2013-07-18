#!/bin/sh

CP="bin/classes"
CP="${CP}:etc"
CP="${CP}:lib/appia-core-4.1.2-no_mm.jar"
CP="${CP}:lib/appia-groupcomm-4.1.2.jar"
CP="${CP}:lib/appia-test-4.1.2.jar"
CP="${CP}:lib/flanagan.jar"
CP="${CP}:lib/jgcs-0.6.1.jar"
CP="${CP}:lib/jgroups-3.3.0.Final.jar"
CP="${CP}:lib/guava-14.0.jar"
CP="${CP}:lib/junit-4.6.jar"
CP="${CP}:lib/jvstm.jar"
CP="${CP}:lib/log4j-1.2.14.jar"
CP="${CP}:lib/spread-4.2.0.jar"
EXCLUDE="java.*,sun.*,org.eclipse.*,org.junit.*,junit.*"
EXCLUDE="${EXCLUDE},net.sf.appia.*"
EXCLUDE="${EXCLUDE},com.google.*"
EXCLUDE="${EXCLUDE},net.sf.jgcs.*"
EXCLUDE="${EXCLUDE},org.jgroups.*"
EXCLUDE="${EXCLUDE},flanagan.*"
EXCLUDE="${EXCLUDE},org.apache.log4j.*"
EXCLUDE="${EXCLUDE},spread.*"
EXCLUDE="${EXCLUDE},org.deuce.trove.*"
EXCLUDE="${EXCLUDE},jstamp.vacation.Random"

SITE=$1
THREADS=$2
REPLICAS=$3
RUN=$4

BENCHMARK=Vacation
TASK_QUERIES=2
PERC_QUERIED=90
PERC_RESERVATION=98
RELATIONS=128 #16384
TASKS=4096
# vacation-high   -n4 -q60 -u90 -r16384   -t4096
# vacation-high+  -n4 -q60 -u90 -r1048576 -t4096
# vacation-high++ -n4 -q60 -u90 -r1048576 -t4194304
# vacation-low    -n2 -q90 -u98 -r16384   -t4096
# vacation-low+   -n2 -q90 -u98 -r1048576 -t4096
# vacation-low++  -n2 -q90 -u98 -r1048576 -t4194304

_STM=score.SCOReContext
_REP=score.SCOReProtocol
_COMM=jgroups.JGroups

STM="org.deuce.transaction.${_STM}"
COMM="org.deuce.distribution.groupcomm.${_COMM}GroupCommunication"
REP="org.deuce.distribution.replication.partial.protocol.${_REP}"
ZIP=true
GROUP="${BENCHMARK}_${TASK_QUERIES}_${PERC_QUERIED}_${PERC_RESERVATION}_${RELATIONS}_${TASKS}_${THREADS}_${_REP}_${REPLICAS}_${RUN}"
FNAME="${BENCHMARK}_n${TASK_QUERIES}_q${PERC_QUERIED}_u${PERC_RESERVATION}_r${RELATIONS}_t${TASKS}_t${THREADS}_${_REP}_${_COMM}_id${SITE}-${REPLICAS}_run${RUN}"
LOG=logs/${FNAME}.res
#DATA_PART=org.deuce.distribution.replication.partitioner.data.RandomDataPartitioner
DATA_PART=org.deuce.distribution.replication.partitioner.data.SimpleDataPartitioner

echo "#####"
echo "Benchmark: ${BENCHMARK}, run ${RUN}"
echo "Threads: ${THREADS}"
echo "Protocol: ${_REP}, site ${SITE} of ${REPLICAS}"
echo "Comm: ${_COMM}"
echo `date +%H:%M`
echo "#####"

REPLICA_TASKS=`expr $TASKS / $REPLICAS`
java -Xmx1g -Xms1g -cp $CP -javaagent:bin/deuceAgent.jar \
	-Dlog=$5 \
	-Dorg.deuce.transaction.contextClass=$STM \
	-Dorg.deuce.exclude=$EXCLUDE \
	-Dtribu.groupcommunication.class=$COMM \
	-Dtribu.groupcommunication.group=$GROUP \
	-Dtribu.site=$SITE \
	-Dorg.deuce.verbose=true \
	-Dtribu.replicas=$REPLICAS \
	-Dtribu.distributed.protocolClass=$REP \
	-Dtribu.serialization.compress=$ZIP \
	-Dtribu.distributed.DataPartitionerClass=$DATA_PART \
	-Dtribu.distributed.PartialReplicationMode=true \
	-Dtribu.groups=2 \
	jstamp.vacation.Vacation -c $THREADS -q $PERC_QUERIED \
		-u $PERC_RESERVATION -r $RELATIONS -t $REPLICA_TASKS \
		-n $TASK_QUERIES
