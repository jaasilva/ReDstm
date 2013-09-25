#!/bin/sh

CP="bin/classes"
CP="${CP}:etc"
CP="${CP}:lib/appia-core-4.1.2.jar"
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
EXCLUDE="java.*,com.sun.*,sun.*,org.eclipse.*,org.junit.*,junit.*"
EXCLUDE="${EXCLUDE},net.sf.appia.*"
EXCLUDE="${EXCLUDE},com.google.*"
EXCLUDE="${EXCLUDE},net.sf.jgcs.*"
EXCLUDE="${EXCLUDE},org.jgroups.*"
EXCLUDE="${EXCLUDE},flanagan.*"
EXCLUDE="${EXCLUDE},org.apache.log4j.*"
EXCLUDE="${EXCLUDE},spread.*"
EXCLUDE="${EXCLUDE},org.deuce.trove.*"
EXCLUDE="${EXCLUDE},org.deuce.distribution.UniqueObject"
EXCLUDE="${EXCLUDE},org.deuce.distribution.replication.partial.oid.PartialReplicationOID"

WARMUP=0
DURATION=10000

SITE=$1
REPLICAS=$2

SIZE=500 # 2^15
RANGE=131072 # SIZE*4
WRITES=10

_STM=score.SCOReContext
_REP=score.SCOReProtocol
_COMM=jgroups.JGroups

STM="org.deuce.transaction.${_STM}"
COMM="org.deuce.distribution.groupcomm.${_COMM}GroupCommunication"
REP="org.deuce.distribution.replication.partial.protocol.${_REP}"
ZIP=true
GROUP="${BENCHMARK}_${SIZE}_${WRITES}_${THREADS}_${_REP}_${REPLICAS}_${RUN}"

FNAME="${BENCHMARK}_i${SIZE}_w${WRITES}_t${THREADS}_${_REP}_${_COMM}_id${SITE}-${REPLICAS}_run${RUN}"
LOG=logs/${FNAME}.res

echo "#####"
echo "Benchmark: Partial List"
echo "Protocol: ${_REP}, site ${SITE} of ${REPLICAS}"
echo "Comm: ${_COMM}"
echo `date +%H:%M`
echo "#####"

java -cp $CP -javaagent:bin/deuceAgent.jar \
	-Dlog=$3 \
	-Dorg.deuce.transaction.contextClass=$STM \
	-Dorg.deuce.exclude=$EXCLUDE \
	-Dtribu.groupcommunication.class=$COMM \
	-Dtribu.groupcommunication.group=$GROUP \
	-Dtribu.site=$SITE \
	-Dtribu.replicas=$REPLICAS \
	-Dtribu.distributed.protocolClass=$REP \
	-Dtribu.serialization.compress=$ZIP \
	-Dtribu.distributed.PartialReplicationMode=true \
	-Dtribu.groups=2 \
	test.partial.Main
