#!/bin/sh

CP="bin/classes"
CP="${CP}:etc"
CP="${CP}:lib/appia-core-4.1.2.jar"
CP="${CP}:lib/appia-groupcomm-4.1.2.jar"
CP="${CP}:lib/appia-test-4.1.2.jar"
CP="${CP}:lib/flanagan.jar"
CP="${CP}:lib/jgcs-0.6.1.jar"
CP="${CP}:lib/jgroups-3.3.0.Final.jar"
CP="${CP}:lib/junit-4.6.jar"
CP="${CP}:lib/jvstm.jar"
CP="${CP}:lib/log4j-1.2.14.jar"
CP="${CP}:lib/spread-4.2.0.jar"
EXCLUDE="java.*,sun.*,org.eclipse.*,org.junit.*,junit.*"
EXCLUDE="${EXCLUDE},net.sf.appia.*"
EXCLUDE="${EXCLUDE},net.sf.jgcs.*"
EXCLUDE="${EXCLUDE},org.jgroups.*"
EXCLUDE="${EXCLUDE},flanagan.*"
EXCLUDE="${EXCLUDE},org.apache.log4j.*"
EXCLUDE="${EXCLUDE},spread.*"
EXCLUDE="${EXCLUDE},org.deuce.trove.*"

WARMUP=0
DURATION=10000

SITE=$3
THREADS=$4
REPLICAS=$5
RUN=$6

BENCHMARK=$1
SIZE=1024 # 2^10
RANGE=4096 # SIZE*4
WRITES=10

_STM=tl2.Context
_REP=nonvoting.NonVoting
_COMM=$2

STM="org.deuce.transaction.${_STM}"
COMM="org.deuce.distribution.groupcomm.${_COMM}GroupCommunication"
REP="org.deuce.distribution.replication.full.protocol.${_REP}"
ZIP=true
GROUP="${BENCHMARK}_${SIZE}_${WRITES}_${THREADS}_${_REP}_${REPLICAS}_${RUN}"

FNAME="${BENCHMARK}_i${SIZE}_w${WRITES}_t${THREADS}_${_REP}_${_COMM}_id${SITE}-${REPLICAS}_run${RUN}"
LOG=logs/${FNAME}.res

echo "#####"
echo "Benchmark: ${BENCHMARK} -i ${SIZE} -w ${WRITES}, run ${RUN}"
echo "Threads: ${THREADS}"
echo "Protocol: ${_REP}, site ${SITE} of ${REPLICAS}"
echo "Comm: ${_COMM}"
echo `date +%H:%M`
echo "#####"

java -Xmx1g -Xms1g -cp $CP -javaagent:bin/deuceAgent.jar \
	-Dlog=$7 \
	-Dorg.deuce.transaction.contextClass=$STM \
	-Dorg.deuce.exclude=$EXCLUDE \
	-Dtribu.groupcommunication.class=$COMM \
	-Dtribu.groupcommunication.group=$GROUP \
	-Dtribu.site=$SITE \
	-Dtribu.replicas=$REPLICAS \
	-Dtribu.distributed.protocolClass=$REP \
	-Dtribu.serialization.compress=$ZIP \
	org.deuce.benchmark.Driver -n $THREADS -d $DURATION -w $WARMUP \
		org.deuce.benchmark.intset.Benchmark $BENCHMARK -r $RANGE -i $SIZE \
		-w $WRITES 
