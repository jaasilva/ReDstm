#!/bin/sh

echo "#####"
echo "Start: `date +'%F %H:%M:%S'`"
echo "#####"

_start=`date +%s`
_replicas=8
_bench=RedBTree

###############################################################################
# PARTIAL REPLICATION
###############################################################################

echo "#######################"
echo "# PARTIAL REPLICATION #"
echo "#######################"

for _thrs in 4
do
for _writes in 10
do
for _groups in 1 2 4 8
do
for _run in 1 2 3 4 5

echo "#####"
echo "Benchmark: ${_bench}, writes: ${_writes}%, run ${_run}"
echo "Threads: ${_thrs}, replicas: ${_replicas}, groups: ${_groups}"
echo "Time: `date +'%F %H:%M:%S'`"
echo "#####"

_start2=`date +%s`

for _node in node1 node2 node3 node4 node5 node6 node7 node8
do
	ssh $_node "cd ./repos/metadata; ./scripts/run/intset-par_rep.sh ${_bench} \
		${_thrs} ${_replicas} ${_run} ${_writes} ${_groups} > $node.out 2>&1" &
done

wait

_end2=`date +%s`
echo "> $(( ($_end2-$_start2) ))s"

sleep 10

done
done
done
done

_end=`date +%s`
echo "$(( ($_end-$_start)/60 ))min"

###############################################################################
# FULL REPLICATION
###############################################################################

echo "####################"
echo "# FULL REPLICATION #"
echo "####################"

for _thrs in 4
do
for _writes in 10
do
for _run in 1 2 3 4 5

echo "#####"
echo "Benchmark: ${_bench}, writes: ${_writes}%, run ${_run}"
echo "Threads: ${_thrs}, replicas: ${_replicas}"
echo "Time: `date +'%F %H:%M:%S'`"
echo "#####"

_start2=`date +%s`

for _node in node1 node2 node3 node4 node5 node6 node7 node8
do
	ssh $_node "cd ./repos/metadata; ./scripts/run/intset-full_rep.sh ${_bench} \
		${_thrs} ${_replicas} ${_run} ${_writes} > $node.out 2>&1" &
done

wait

_end2=`date +%s`
echo "> $(( ($_end2-$_start2) ))s"

sleep 10

done
done
done

echo "#####"
echo "End: `date +'%F %H:%M:%S'`"
echo "#####"

_end=`date +%s`
echo "Duration: $(( ($_end-$_start)/60 ))min"

# Bash block comment
:<<'END'
END

