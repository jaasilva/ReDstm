#!/bin/bash

echo "#####"
echo "Start: `date +'%F %H:%M:%S'`"
echo "#####"

_start=`date +%s`
_runs=5
_thrs=4

:<<'END'
END

_replicas=2
_groups=1
echo "#######################"
echo "### REPLICAS: ${_replicas} (${_groups} groups)"
echo "#######################"

for _reads in 90 50
do
for _run in `seq 1 $_runs`
do
for _cache in batch eager lazy
do

echo "#####"
echo "Benchmark: Vacation, reads: ${_reads}%, run ${_run}"
echo "Threads: ${_thrs}, replicas: ${_replicas}, groups: ${_groups}, cache: ${_cache}"
echo "Time: `date +'%F %H:%M:%S'`"
echo "#####"

_start2=`date +%s`
pids=()
i=0

for _node in node1 node6
do
	ssh $_node "cd ./repos/pardstm; ./scripts/run/vacation-par_rep-cache.sh ${_thrs} \
	  ${_replicas} ${_run} ${_groups} ${_reads} \
	  ${_cache} > $_node.out 2>&1" & pids[$i]=$!
	(( i++ ))
done

# launch watchdog
./scripts/misc/watchdog.sh $_reads 0 $_run $_groups $_cache & _PID2=$!

# wait for nodes to finish
for p in ${pids[@]}
do
	wait $p
done

# if in time, kill watchdog
kill $_PID2 2> /dev/null

_end2=`date +%s`
echo "> $(( ($_end2-$_start2) ))s"

sleep 10

done
done
done

_end=`date +%s`
echo "$(( ($_end-$_start)/60 ))min"

###############################################################################
_replicas=4
_groups=2
echo "#######################"
echo "### REPLICAS: ${_replicas} (${_groups} groups)"
echo "#######################"

for _reads in 90 50
do
for _run in `seq 1 $_runs`
do
for _cache in batch eager lazy
do

echo "#####"
echo "Benchmark: Vacation, reads: ${_reads}%, run ${_run}"
echo "Threads: ${_thrs}, replicas: ${_replicas}, groups: ${_groups}, cache: ${_cache}"
echo "Time: `date +'%F %H:%M:%S'`"
echo "#####"

_start2=`date +%s`
pids=()
i=0

for _node in node1 node2 node6 node7
do
	ssh $_node "cd ./repos/pardstm; ./scripts/run/vacation-par_rep-cache.sh ${_thrs} \
	  ${_replicas} ${_run} ${_groups} ${_reads} \
	  ${_cache} > $_node.out 2>&1" & pids[$i]=$!
	(( i++ ))
done

# launch watchdog
./scripts/misc/watchdog.sh $_reads 0 $_run $_groups $_cache & _PID2=$!

# wait for nodes to finish
for p in ${pids[@]}
do
	wait $p
done

# if in time, kill watchdog
kill $_PID2 2> /dev/null

_end2=`date +%s`
echo "> $(( ($_end2-$_start2) ))s"

sleep 10

done
done
done

_end=`date +%s`
echo "$(( ($_end-$_start)/60 ))min"

###############################################################################
_replicas=6
_groups=3
echo "#######################"
echo "### REPLICAS: ${_replicas} (${_groups} groups)"
echo "#######################"

for _reads in 90 50
do
for _run in `seq 1 $_runs`
do
for _cache in batch eager lazy
do

echo "#####"
echo "Benchmark: Vacation, reads: ${_reads}%, run ${_run}"
echo "Threads: ${_thrs}, replicas: ${_replicas}, groups: ${_groups}, cache: ${_cache}"
echo "Time: `date +'%F %H:%M:%S'`"
echo "#####"

_start2=`date +%s`
pids=()
i=0

for _node in node1 node2 node3 node6 node7 node8
do
	ssh $_node "cd ./repos/pardstm; ./scripts/run/vacation-par_rep-cache.sh ${_thrs} \
	  ${_replicas} ${_run} ${_groups} ${_reads} \
	  ${_cache} > $_node.out 2>&1" & pids[$i]=$!
	(( i++ ))
done

# launch watchdog
./scripts/misc/watchdog.sh $_reads 0 $_run $_groups $_cache & _PID2=$!

# wait for nodes to finish
for p in ${pids[@]}
do
	wait $p
done

# if in time, kill watchdog
kill $_PID2 2> /dev/null

_end2=`date +%s`
echo "> $(( ($_end2-$_start2) ))s"

sleep 10

done
done
done

_end=`date +%s`
echo "$(( ($_end-$_start)/60 ))min"

###############################################################################
_replicas=8
_groups=4
echo "#######################"
echo "### REPLICAS: ${_replicas} (${_groups} groups)"
echo "#######################"

for _reads in 90 50
do
for _run in `seq 1 $_runs`
do
for _cache in batch eager lazy
do

echo "#####"
echo "Benchmark: Vacation, reads: ${_reads}%, run ${_run}"
echo "Threads: ${_thrs}, replicas: ${_replicas}, groups: ${_groups}, cache: ${_cache}"
echo "Time: `date +'%F %H:%M:%S'`"
echo "#####"

_start2=`date +%s`
pids=()
i=0

for _node in node1 node2 node3 node4 node5 node6 node7 node8
do
	ssh $_node "cd ./repos/pardstm; ./scripts/run/vacation-par_rep-cache.sh ${_thrs} \
	  ${_replicas} ${_run} ${_groups} ${_reads} \
	  ${_cache} > $_node.out 2>&1" & pids[$i]=$!
	(( i++ ))
done

# launch watchdog
./scripts/misc/watchdog.sh $_reads 0 $_run $_groups $_cache & _PID2=$!

# wait for nodes to finish
for p in ${pids[@]}
do
	wait $p
done

# if in time, kill watchdog
kill $_PID2 2> /dev/null

_end2=`date +%s`
echo "> $(( ($_end2-$_start2) ))s"

sleep 10

done
done
done

_end=`date +%s`
echo "Duration: $(( ($_end-$_start)/60 ))min"

