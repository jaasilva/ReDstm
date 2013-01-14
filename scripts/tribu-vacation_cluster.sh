#!/bin/sh

CP="bin/classes"
CP="${CP}:etc"
CP="${CP}:lib/appia-core-4.1.2-no_mm.jar"
CP="${CP}:lib/appia-groupcomm-4.1.2.jar"
CP="${CP}:lib/appia-test-4.1.2.jar"
CP="${CP}:lib/flanagan.jar"
CP="${CP}:lib/jgcs-0.6.1.jar"
CP="${CP}:lib/jgroups-3.1.0.Beta1.jar"
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

SITE=$2
THREADS=$3
REPLICAS=$4
RUN=$5

BENCHMARK=Vacation
TASK_QUERIES=2
PERC_QUERIED=90
PERC_RESERVATION=98
RELATIONS=16384
TASKS=4096
# vacation-high   -n4 -q60 -u90 -r16384   -t4096
# vacation-high+  -n4 -q60 -u90 -r1048576 -t4096
# vacation-high++ -n4 -q60 -u90 -r1048576 -t4194304
# vacation-low    -n2 -q90 -u98 -r16384   -t4096
# vacation-low+   -n2 -q90 -u98 -r1048576 -t4096
# vacation-low++  -n2 -q90 -u98 -r1048576 -t4194304

_STM=tl2.Context
_REP=nonvoting.NonVoting
_COMM=$1

STM="org.deuce.transaction.${_STM}"
COMM="org.deuce.distribution.groupcomm.${_COMM}GroupCommunication"
REP="org.deuce.distribution.replication.full.protocol.${_REP}"
ZIP=true
GROUP="${BENCHMARK}_${TASK_QUERIES}_${PERC_QUERIED}_${PERC_RESERVATION}_${RELATIONS}_${TASKS}_${THREADS}_${_REP}_${REPLICAS}_${RUN}"
FNAME="${BENCHMARK}_n${TASK_QUERIES}_q${PERC_QUERIED}_u${PERC_RESERVATION}_r${RELATIONS}_t${TASKS}_t${THREADS}_${_REP}_${_COMM}_id${SITE}-${REPLICAS}_run${RUN}"
LOG=logs/${FNAME}.res

echo "#####"
echo "Benchmark: ${BENCHMARK}, run ${RUN}"
echo "Threads: ${THREADS}"
echo "Protocol: ${_REP}, site ${SITE} of ${REPLICAS}"
echo "Comm: ${_COMM}"
echo `date +%H:%M`
echo "#####"

REPLICA_TASKS=`expr $TASKS / $REPLICAS`
java -Xmx8g -Xms8g -cp $CP -javaagent:bin/deuceAgent.jar \
	-Dorg.deuce.transaction.contextClass=$STM \
	-Dorg.deuce.exclude=$EXCLUDE \
	-Dtribu.groupcommunication.class=$COMM \
	-Dtribu.groupcommunication.group=$GROUP \
	-Dtribu.site=$SITE \
	-Dtribu.replicas=$REPLICAS \
	-Dtribu.distributed.protocolClass=$REP \
	-Dtribu.serialization.compress=$ZIP \
	jstamp.vacation.Vacation -c $THREADS -q $PERC_QUERIED \
		-u $PERC_RESERVATION -r $RELATIONS -t $REPLICA_TASKS \
		-n $TASK_QUERIES >$LOG
