#!/bin/sh

CP="bin/classes"
CP="${CP}:etc"
CP="${CP}:lib/appia-core-4.1.2.jar"
CP="${CP}:lib/appia-groupcomm-4.1.2.jar"
CP="${CP}:lib/appia-test-4.1.2.jar"
CP="${CP}:lib/flanagan.jar"
CP="${CP}:lib/jgcs-0.6.1.jar"
CP="${CP}:lib/jgroups-3.1.0.Beta1.jar"
CP="${CP}:lib/junit-4.6.jar"
CP="${CP}:lib/jvstm.jar"
CP="${CP}:lib/log4j-1.2.14.jar"
CP="${CP}:lib/spread-4.1.0.jar"
EXCLUDE="java.*,sun.*,org.eclipse.*,org.junit.*,junit.*"
EXCLUDE="${EXCLUDE},net.sf.appia.*"
EXCLUDE="${EXCLUDE},net.sf.jgcs.*"
EXCLUDE="${EXCLUDE},org.jgroups.*"
EXCLUDE="${EXCLUDE},flanagan.*"
EXCLUDE="${EXCLUDE},org.apache.log4j.*"
EXCLUDE="${EXCLUDE},spread.*"

WARMUP=0
DURATION=10000
RUNS=5

benchmark=Bank
for run in $(seq 1 $RUNS); do
#for size in 50000; do
for naccounts in 256; do
for writes in 10; do
for reads in 10; do
for threads in 1 2 4; do

	for ctx in tl2.Context; do
	for rep in nonvoting.NonVoting; do
	for comm in jgroups.JGroups appia.Appia; do

		RANGE=1024

		STM="org.deuce.transaction.${ctx}"
		N_THREADS=${threads}
		COMM="org.deuce.distribution.groupcomm.${comm}GroupCommunication"
		PROTO="org.deuce.distribution.replication.full.protocol.${rep}"
		NACCOUNTS=${naccounts}
		WRITE=${writes}
		READ=${reads}
		SITE=${1}
		REPLICAS=${2}
		ZIP=false
		GROUP="${benchmark}_${naccounts}_${reads}_${writes}_${threads}_${rep}_${REPLICAS}_${run}"

		FNAME="${benchmark}_i${size}_w${writes}_t${threads}_${rep}_${comm}_id${SITE}-${REPLICAS}_run${run}"
		LOG=logs/${FNAME}.res

		echo "#####"
		echo "Benchmark: ${benchmark} -n ${naccounts} -r ${reads} -w ${writes}, run ${run} of ${RUNS}"
		echo "Threads: ${threads}"
		echo "Protocol: ${rep}, site ${SITE} of ${REPLICAS}"
		echo "Comm: ${comm}"
		echo `date +%H:%M`
		echo "#####"

		java -Xmx4g -Xms4g -cp ${CP} -javaagent:bin/deuceAgent.jar \
			-Dorg.deuce.transaction.contextClass=${STM} \
			-Dorg.deuce.exclude=${EXCLUDE} \
			-Dtribu.groupcommunication.class=${COMM} \
			-Dtribu.groupcommunication.group=${GROUP} \
			-Dtribu.site=${SITE} \
			-Dtribu.replicas=${REPLICAS} \
			-Dtribu.distributed.protocolClass=${PROTO} \
			-Dtribu.serialization.compress=${ZIP} \
			org.deuce.benchmark.Driver -n $N_THREADS -d $DURATION -w $WARMUP \
			org.deuce.benchmark.bank.Benchmark -n $NACCOUNTS -r $READ -w $WRITE

	done		
	done
	done
	
done
done
done
done
done
