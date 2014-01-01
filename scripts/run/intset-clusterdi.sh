#!/bin/sh
###############################################################################
# <benchmark>
###############################################################################

echo "#####"
echo "Start: `date +'%F %H:%M:%S'`"
echo "#####"

_start=`date +%s`
_replicas=8
_bench=RedBTreeZ
#_bench=$1
_runs=5

_partial_rep=false
#_partial_rep=true
#_full_rep=false
_full_rep=true

###############################################################################
# PARTIAL REPLICATION
###############################################################################
if $_partial_rep ; then

echo "#######################"
echo "# PARTIAL REPLICATION #"
echo "#######################"

for _thrs in 2 4
do
for _writes in 10 20 50 80 100
do
for _groups in 1 2 4 8
do
for _partial_ops in 0 10 20 50 80 100
do
for _run in `seq 1 $_runs`
do

echo "#####"
echo "Benchmark: ${_bench}, writes: ${_writes}% (partial: ${_partial_ops}%), run ${_run}"
echo "Threads: ${_thrs}, replicas: ${_replicas}, groups: ${_groups}"
echo "Time: `date +'%F %H:%M:%S'`"
echo "#####"

_start2=`date +%s`

for _node in node1 node2 node3 node4 node5 node6 node7 node8
do
	ssh $_node "cd ./repos/pardstm; ./scripts/run/intset-par_rep.sh ${_bench} \
		${_thrs} ${_replicas} ${_run} ${_writes} ${_groups} ${_partial_ops} > $_node.out 2>&1" &
done

wait

_end2=`date +%s`
echo "> $(( ($_end2-$_start2) ))s"

sleep 10

done
done
done
done
done

_end=`date +%s`
echo "$(( ($_end-$_start)/60 ))min"

fi # end partial_rep

###############################################################################
# FULL REPLICATION
###############################################################################
if $_full_rep ; then

echo "####################"
echo "# FULL REPLICATION #"
echo "####################"

for _thrs in 2 4
do
for _writes in 10 20 50 80 100
do
for _partial_ops in 0 10 20 50 80 100
do
for _run in `seq 1 $_runs`
do

echo "#####"
echo "Benchmark: ${_bench}, writes: ${_writes}% (partial: ${_partial_ops}%), run ${_run}"
echo "Threads: ${_thrs}, replicas: ${_replicas}"
echo "Time: `date +'%F %H:%M:%S'`"
echo "#####"

_start2=`date +%s`

for _node in node1 node2 node3 node4 node5 node6 node7 node8
do
	ssh $_node "cd ./repos/pardstm; ./scripts/run/intset-full_rep.sh ${_bench} \
		${_thrs} ${_replicas} ${_run} ${_writes} ${_partial_ops} > $_node.out 2>&1" &
done

wait

_end2=`date +%s`
echo "> $(( ($_end2-$_start2) ))s"

sleep 10

done
done
done
done

fi # end full_rep

echo "#####"
echo "End: `date +'%F %H:%M:%S'`"
echo "#####"

_end=`date +%s`
echo "Duration: $(( ($_end-$_start)/60 ))min"

# Bash block comment
:<<'END'
END

