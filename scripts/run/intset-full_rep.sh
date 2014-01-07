#!/bin/sh
###############################################################################
# <benchmark> <threads> <replicas> <run> <writes> <partial_ops>
###############################################################################

if [ $1 = "-h" ]; then
	echo "<benchmark> <threads> <replicas> <run> <writes> <partial_ops>"
	exit
fi

_CP="bin/classes"
_CP="${_CP}:etc"
_CP="${_CP}:lib/appia-core-4.1.2.jar"
_CP="${_CP}:lib/appia-groupcomm-4.1.2.jar"
_CP="${_CP}:lib/appia-test-4.1.2.jar"
_CP="${_CP}:lib/flanagan.jar"
_CP="${_CP}:lib/jgcs-0.6.1.jar"
_CP="${_CP}:lib/jgroups-3.4.1.Final.jar"
_CP="${_CP}:lib/junit-4.6.jar"
_CP="${_CP}:lib/log4j-1.2.14.jar"
_CP="${_CP}:lib/spread-4.2.0.jar"

_EXCLUDE="java.*,sun.*,org.eclipse.*,org.junit.*,junit.*,com.sun.*"
_EXCLUDE="${_EXCLUDE},net.sf.appia.*"
_EXCLUDE="${_EXCLUDE},net.sf.jgcs.*"
_EXCLUDE="${_EXCLUDE},org.jgroups.*"
_EXCLUDE="${_EXCLUDE},flanagan.*"
_EXCLUDE="${_EXCLUDE},org.apache.log4j.*"
_EXCLUDE="${_EXCLUDE},spread.*"
_EXCLUDE="${_EXCLUDE},org.deuce.trove.*"
_EXCLUDE="${_EXCLUDE},org.deuce.benchmark.intset.MyObjectBackend"

_BENCH_BIG=false
#_BENCH_BIG=true

if $_BENCH_BIG ; then # big values
	_SIZE=32768 # 2^15
	_RANGE=131072 # SIZE*4
else # small values
	_SIZE=1024 # 2^10
	_RANGE=4096 # SIZE*4
fi

_WARMUP=0
#_DURATION=10000 # s
_DURATION=30000 # s

_SITE=`hostname | cut -c 5-`
_THREADS=$2
_REPLICAS=$3
_RUN=$4

_BENCH=$1
_WRITES=$5
_PARTIAL_OPS=$6

_CTX=tl2.Context
#_CTX=mvstm.Context
_PROTO=nonvoting.NonVoting
#_PROTO=voting.Voting
_GCS=jgroups.JGroups
#_GCS=appia.Appia
#_GCS=spread.Spread

_STM="org.deuce.transaction.${_CTX}"
_COMM="org.deuce.distribution.groupcomm.${_GCS}GroupCommunication"
_REP="org.deuce.distribution.replication.full.protocol.${_PROTO}"

_GROUPCOMM="${_BENCH}_${_SIZE}_${_WRITES}_${_THREADS}_${_PROTO}_${_CTX}_${_REPLICAS}_${_RUN}_${_PARTIAL_OPS}"

_FNAME="${_BENCH}_i${_SIZE}_w${_WRITES}_t${_THREADS}_${_PROTO}_${_CTX}_${_GCS}_id${_SITE}-${_REPLICAS}_run${_RUN}_${_PARTIAL_OPS}"

_LOG=logs/${_FNAME}.res
_MEM=${_LOG}.mem
_PROFILE_MEM=false
#_PROFILE_MEM=true

echo "#####"
echo "Benchmark: ${_BENCH} -i ${_SIZE} -w ${_WRITES}, run ${_RUN}"
echo "Threads: ${_THREADS}, site ${_SITE} of ${_REPLICAS}"
echo "Protocol: ${_PROTO}, context: ${_CTX}"
echo "Comm: ${_GCS}"
echo "Start: `date +'%F %H:%M:%S'`"
echo "#####"

if $_PROFILE_MEM ; then
#	dstat -m -M top-mem > $_MEM & # in my pc
	dstat -m -M topmem > $_MEM &
	_PID2=$!
	sleep 1
fi

java -Xmx8g -cp $_CP -javaagent:bin/deuceAgent.jar \
	-Dorg.deuce.transaction.contextClass=$_STM \
	-Dorg.deuce.exclude=$_EXCLUDE \
	-Dtribu.groupcommunication.class=$_COMM \
	-Dtribu.groupcommunication.group=$_GROUPCOMM \
	-Dtribu.replicas=$_REPLICAS \
	-Dtribu.distributed.protocolClass=$_REP \
	-Djgroups.bind_addr=`hostname` \
	-Djava.net.preferIPv4Stack=true \
	org.deuce.benchmark.Driver -n $_THREADS -d $_DURATION -w $_WARMUP \
		org.deuce.benchmark.intset.Benchmark $_BENCH -r $_RANGE -i $_SIZE \
		-w $_WRITES -po $_PARTIAL_OPS 2>&1 | tee $_LOG

echo "#####"
echo "End: `date +'%F %H:%M:%S'`"
echo "#####"

if $_PROFILE_MEM ; then
	sleep 1
	kill $_PID2
	wait $_PID2 2> /dev/null
fi

