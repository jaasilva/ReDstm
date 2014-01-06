#!/bin/sh
###############################################################################
# <partial_rep> <full_rep>
###############################################################################

echo "#####"
echo "Start: `date +'%F %H:%M:%S'`"
echo "#####"

_start=`date +%s`
_replicas=8
_bench=Vacation
_runs=10

_partial_rep=false
#_partial_rep=true
#_partial_rep=$1
#_full_rep=$2
#_full_rep=false
_full_rep=true

###############################################################################
# PARTIAL REPLICATION
###############################################################################
if $_partial_rep ; then

echo "#######################"
echo "# PARTIAL REPLICATION #"
echo "#######################"

for _thrs in 4
do
for _groups in 1 2 4 8
do
for _run in `seq 1 $_runs`
do

echo "#####"
echo "Benchmark: ${_bench}, run ${_run}"
echo "Threads: ${_thrs}, replicas: ${_replicas}, groups: ${_groups}"
echo "Time: `date +'%F %H:%M:%S'`"
echo "#####"

_start2=`date +%s`

for _node in node1 node2 node3 node4 node5 node6 node7 node8
do
	ssh $_node "cd ./repos/pardstm; ./scripts/run/vacation-par_rep.sh \
		${_thrs} ${_replicas} ${_run} ${_groups} > $node.out 2>&1" &
done

wait

_end2=`date +%s`
echo "> $(( ($_end2-$_start2) ))s"

sleep 10

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

for _thrs in 4
do
for _run in `seq 1 $_runs`
do

echo "#####"
echo "Benchmark: ${_bench}, run ${_run}"
echo "Threads: ${_thrs}, replicas: ${_replicas}"
echo "Time: `date +'%F %H:%M:%S'`"
echo "#####"

_start2=`date +%s`

for _node in node1 node2 node3 node4 node5 node6 node7 node8
do
	ssh $_node "cd ./repos/pardstm; ./scripts/run/vacation-full_rep.sh \
		${_thrs} ${_replicas} ${_run} > $node.out 2>&1" &
done

wait

_end2=`date +%s`
echo "> $(( ($_end2-$_start2) ))s"

sleep 10

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

