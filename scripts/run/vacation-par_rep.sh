#!/bin/bash
###############################################################################
# <threads> <replicas> <run> <groups> <reads>
###############################################################################

if [ $1 = "-h" ]; then
	echo "<threads> <replicas> <run> <groups> <reads>"
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
_EXCLUDE="${_EXCLUDE},jstamp.vacation.Random"

_BENCH=Vacation
_THREADS=$1
_REPLICAS=$2
_RUN=$3
_GROUPS=$4

#_VACATION_HIGH=true
_VACATION_HIGH=false

if $_VACATION_HIGH ; then # vacation-high
	_TASK_QUERIES=4
	_PERC_QUERIED=60
	_PERC_RESERVATION=90
else # vacation-low
	_TASK_QUERIES=2
	_PERC_QUERIED=90
	_PERC_RESERVATION=98
fi

_USER_CONSULT=$5
#_RELATIONS=1048576
_RELATIONS=16384
#_TASKS=4194304
_TASKS=4096

# vacation-high   -n4 -q60 -u90 -r16384   -t4096
# vacation-high+  -n4 -q60 -u90 -r1048576 -t4096
# vacation-high++ -n4 -q60 -u90 -r1048576 -t4194304
# vacation-low    -n2 -q90 -u98 -r16384   -t4096
# vacation-low+   -n2 -q90 -u98 -r1048576 -t4096
# vacation-low++  -n2 -q90 -u98 -r1048576 -t4194304

_CTX=score.Context
_PROTO=score.SCORe
_GCS=jgroups.JGroups
_DPART=RoundRobin
_CACHE=false

_STM="org.deuce.transaction.${_CTX}"
_COMM="org.deuce.distribution.groupcomm.${_GCS}GroupCommunication"
_REP="org.deuce.distribution.replication.partial.protocol.${_PROTO}"
_DATAPART="org.deuce.distribution.replication.partitioner.data.${_DPART}DataPartitioner"

_GROUPCOMM="${_BENCH}_${_TASK_QUERIES}_${_PERC_QUERIED}_${_PERC_RESERVATION}_${_USER_CONSULT}_${_RELATIONS}_${_TASKS}_${_THREADS}_${_PROTO}_${_CTX}_${_REPLICAS}_${_RUN}"

_SITE=`hostname | cut -c 5-`
_FNAME="${_BENCH}_n${_TASK_QUERIES}_q${_PERC_QUERIED}_u${_PERC_RESERVATION}_uc${_USER_CONSULT}_r${_RELATIONS}_t${_TASKS}_t${_THREADS}_${_PROTO}_${_CTX}_${_GCS}_id${_SITE}-${_REPLICAS}_run${_RUN}_g${_GROUPS}_${_DPART}"

_LOG=logs/${_FNAME}.res

echo "#####"
echo "Benchmark: ${_BENCH} -r ${_USER_CONSULT}, run ${_RUN}"
echo "Threads: ${_THREADS}, replicas: ${_REPLICAS}, groups: ${_GROUPS}"
echo "Protocol: ${_PROTO}, context: ${_CTX}"
echo "Comm: ${_GCS}, data partitioner: ${_DPART}"
echo "Start: `date +'%F %H:%M:%S'`"
echo "#####"
_start2=`date +%s`

_REPLICA_TASKS=`expr $_TASKS / $_REPLICAS`
java -Xmx8g -cp $_CP -javaagent:bin/deuceAgent.jar \
	-Dorg.deuce.transaction.contextClass=$_STM \
	-Dorg.deuce.exclude=$_EXCLUDE \
	-Dtribu.groupcommunication.class=$_COMM \
	-Dtribu.groupcommunication.group=$_GROUPCOMM \
	-Dtribu.replicas=$_REPLICAS \
	-Dtribu.distributed.protocolClass=$_REP \
	-Dtribu.distributed.DataPartitionerClass=$_DATAPART \
	-Dtribu.distributed.PartialReplicationMode=true \
	-Dtribu.groups=$_GROUPS \
	-Djgroups.bind_addr=`hostname` \
	-Djava.net.preferIPv4Stack=true \
	-Dlog=${_SITE} \
	-Dtribu.distributed.partial.cache=$_CACHE \
	jstamp.vacation.Vacation -c $_THREADS -q $_PERC_QUERIED \
		-u $_PERC_RESERVATION -r $_RELATIONS -t $_REPLICA_TASKS \
		-n $_TASK_QUERIES -uc $_USER_CONSULT 2>&1 | tee $_LOG

_end2=`date +%s`
echo "#####"
echo "End: `date +'%F %H:%M:%S'` $(( ($_end2-$_start2) ))s"
echo "#####"

