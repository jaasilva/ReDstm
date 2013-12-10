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
EXCLUDE="java.*,sun.*,org.eclipse.*,org.junit.*,junit.*"
EXCLUDE="${EXCLUDE},net.sf.appia.*"
EXCLUDE="${EXCLUDE},com.google.*"
EXCLUDE="${EXCLUDE},net.sf.jgcs.*"
EXCLUDE="${EXCLUDE},org.jgroups.*"
EXCLUDE="${EXCLUDE},flanagan.*"
EXCLUDE="${EXCLUDE},org.apache.log4j.*"
EXCLUDE="${EXCLUDE},spread.*"
EXCLUDE="${EXCLUDE},org.deuce.trove.*"
EXCLUDE="${EXCLUDE},org.deuce.benchmark.intset.MyObjectBackend"

WARMUP=0
DURATION=10000

SITE=`hostname | cut -c 5-`
THREADS=$2
REPLICAS=$3
RUN=$4

BENCHMARK=$1
SIZE=1024 #32768 #1024 #32768 # 2^15
RANGE=4096 #131072 #4096 #131072 # SIZE*4
WRITES=$5

_STM=tl2.Context
_REP=nonvoting.NonVoting
_COMM=jgroups.JGroups

STM="org.deuce.transaction.${_STM}"
COMM="org.deuce.distribution.groupcomm.${_COMM}GroupCommunication"
REP="org.deuce.distribution.replication.full.protocol.${_REP}"
ZIP=true
GROUP="${BENCHMARK}_${SIZE}_${WRITES}_${THREADS}_${_REP}_${REPLICAS}_${RUN}"
FNAME="${BENCHMARK}_i${SIZE}_w${WRITES}_t${THREADS}_${_REP}_${_COMM}_id${SITE}-${REPLICAS}_run${RUN}"
LOG=logs/${FNAME}.res
MEM=${LOG}.mem

echo "#####"
echo "Benchmark: ${BENCHMARK} -i ${SIZE} -w ${WRITES}, run ${RUN}"
echo "Threads: ${THREADS}"
echo "Protocol: ${_REP}, site ${SITE} of ${REPLICAS}"
echo "Comm: ${_COMM}"
echo `date +'%F %H:%M:%S'`
echo "#####"

#dstat -m -M topmem > $MEM &
#PID2=$!
#sleep 1
#-Xmx8g -Xms8g
java -Xmx8g -Xms8g -cp $CP -javaagent:bin/deuceAgent.jar \
	-Dorg.deuce.transaction.contextClass=$STM \
	-Dorg.deuce.exclude=$EXCLUDE \
	-Dtribu.groupcommunication.class=$COMM \
	-Dtribu.groupcommunication.group=$GROUP \
	-Dtribu.replicas=$REPLICAS \
	-Dtribu.distributed.protocolClass=$REP \
	-Dtribu.serialization.compress=$ZIP \
	-Djgroups.bind_addr=`hostname` \
	-Djava.net.preferIPv4Stack=true \
	org.deuce.benchmark.Driver -n $THREADS -d $DURATION -w $WARMUP \
		org.deuce.benchmark.intset.Benchmark $BENCHMARK -r $RANGE -i $SIZE \
		-w $WRITES > $LOG

echo "ended: `date +'%F %H:%M:%S'`"
#sleep 1
#kill $PID2 
#wait $PID2 2> /dev/null

