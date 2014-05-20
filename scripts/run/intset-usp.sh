#!/bin/bash

echo "#####"
echo "Start: `date +'%F %H:%M:%S'`"
echo "#####"

_start=`date +%s`
_replicas=10
_bench=RedBTreeZ
_runs=10

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

for _thrs in 2
do
for _writes in 10 20 50 80
do
for _groups in 5 #1 2 4 8
do
for _partial_ops in 0 10 50 80 100
do
for _run in `seq 1 $_runs`
do

echo "#####"
echo "Benchmark: ${_bench}, writes: ${_writes}% (partial: ${_partial_ops}%), run ${_run}"
echo "Threads: ${_thrs}, replicas: ${_replicas}, groups: ${_groups}"
echo "Time: `date +'%F %H:%M:%S'`"
echo "#####"

_start2=`date +%s`
pids=()
i=0

# launch jobs in nodes
for _node in node2-vm1 \\ #node2-vm2 node2-vm3 node2-vm4 \\
  node3-vm1 \\ #node3-vm2 node3-vm3 node3-vm4 \\
  node4-vm1 \\ #node4-vm2 node4-vm3 node4-vm4 \\
  node5-vm1 \\ #node5-vm2 node5-vm3 node5-vm4 \\
  node6-vm1 \\ #node6-vm2 node6-vm3 node6-vm4 \\
  node7-vm1 \\ #node7-vm2 node7-vm3 node7-vm4 \\
  node8-vm1 \\ #node8-vm2 node8-vm3 node8-vm4 \\
  node9-vm1 \\ #node9-vm2 node9-vm3 node9-vm4 \\
  node10-vm1 \\ #node10-vm2 node10-vm3 node10-vm4 \\
  node11-vm1 #node11-vm2 node11-vm3 node11-vm4 \\
  #node12-vm1 node12-vm2 node12-vm3 node12-vm4 \\
  #node15-vm1 node15-vm2 node15-vm3 node15-vm4 \\
  #node16-vm1 node16-vm2 node16-vm3 node16-vm4 \\
  #node17-vm1 node17-vm2 node17-vm3 node17-vm4 \\
  #node18-vm1 node18-vm2 node18-vm3 node18-vm4
do
	ssh $_node "cd ./rdstm; ./scripts/run/intset-par_rep.sh ${_bench} \
		${_thrs} ${_replicas} ${_run} ${_writes} ${_groups} \
		${_partial_ops} > $_node.out 2>&1" & pids[$i]=$!
	(( i++ ))
done

# launch watchdog
./scripts/misc/watchdog.sh $_writes $_partial_ops $_run $_groups & _PID2=$!

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
done
done

_end=`date +%s`
echo "$(( ($_end-$_start)/60 ))min"

fi # end partial_rep

echo "" >> error

###############################################################################
# FULL REPLICATION
###############################################################################
if $_full_rep ; then

echo "####################"
echo "# FULL REPLICATION #"
echo "####################"

for _thrs in 2
do
for _writes in 10 20 50 80
do
for _partial_ops in 0 10 50 80 100
do
for _run in `seq 1 $_runs`
do

echo "#####"
echo "Benchmark: ${_bench}, writes: ${_writes}% (partial: ${_partial_ops}%), run ${_run}"
echo "Threads: ${_thrs}, replicas: ${_replicas}"
echo "Time: `date +'%F %H:%M:%S'`"
echo "#####"

_start2=`date +%s`
pids=()
i=0

# launch jobs in nodes
for _node in node2-vm1 \\ #node2-vm2 node2-vm3 node2-vm4 \\
  node3-vm1 \\ #node3-vm2 node3-vm3 node3-vm4 \\
  node4-vm1 \\ #node4-vm2 node4-vm3 node4-vm4 \\
  node5-vm1 \\ #node5-vm2 node5-vm3 node5-vm4 \\
  node6-vm1 \\ #node6-vm2 node6-vm3 node6-vm4 \\
  node7-vm1 \\ #node7-vm2 node7-vm3 node7-vm4 \\
  node8-vm1 \\ #node8-vm2 node8-vm3 node8-vm4 \\
  node9-vm1 \\ #node9-vm2 node9-vm3 node9-vm4 \\
  node10-vm1 \\ #node10-vm2 node10-vm3 node10-vm4 \\
  node11-vm1 #node11-vm2 node11-vm3 node11-vm4 \\
  #node12-vm1 node12-vm2 node12-vm3 node12-vm4 \\
  #node15-vm1 node15-vm2 node15-vm3 node15-vm4 \\
  #node16-vm1 node16-vm2 node16-vm3 node16-vm4 \\
  #node17-vm1 node17-vm2 node17-vm3 node17-vm4 \\
  #node18-vm1 node18-vm2 node18-vm3 node18-vm4
do
	ssh $_node "cd ./rdstm; ./scripts/run/intset-full_rep.sh ${_bench} \
		${_thrs} ${_replicas} ${_run} ${_writes} \
		${_partial_ops} > $_node.out 2>&1" & pids[$i]=$!
	(( i++ ))
done

# launch watchdog
./scripts/misc/watchdog.sh $_writes $_partial_ops $_run 0 & _PID2=$!

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

