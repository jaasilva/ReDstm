echo "START: `date +%H:%M:%S`"
start=`date +%s`

for comm in jgroups.JGroups #appia.Appia spread.Spread
do
for bench in RedBTreeW
do 
for threads in 4
do
for _data in Random #RoundRobin #Simple  Random
do
for writes in 0
do
for _groups in 1 2 4 8
do
for run in 1 2 3 4 5
do

echo "###########################"
echo "Benchmark: $bench, run $run"
echo "Thrs: $threads, Rpls: 8, Groups: $_groups"
echo "Comm: $comm, write: $writes%"
echo "DPart: $_data"
echo "Time: `date +%H:%M:%S`"
echo "###########################"

start2=`date +%s`

for node in node1 node2 node3 node4 node5 node6 node7 node8
do
	ssh $node "cd ./repos/metadata; ./scripts/tribu-intset_cluster_score.sh $bench $threads 8 $_groups $run $_data $writes > $node.out 2>&1" &
done

wait

end2=`date +%s`
echo "> $(( ($end2-$start2) ))s"

sleep 10

done
done
done
done
done
done
done

############################################################################
echo "-----------------------------------------------------------"
:<<'END'
for comm in jgroups.JGroups #appia.Appia spread.Spread
do
for bench in RedBTreeW
do
for threads in 4
do
for writes in 0
do
for run in 1 2 3 4 5
do

echo "###########################"
echo "Benchmark: $bench, run $run"
echo "Thrs: $threads, Rpls: 8"
echo "Comm: $comm, write: $writes%"
echo "Time: `date +%H:%M:%S`"
echo "###########################"

start2=`date +%s`

for node in node1 node2 node3 node4 node5 node6 node7 node8
do
	ssh $node "cd ./repos/metadata; ./scripts/tribu-intset_cluster.sh $bench $threads 8 $run $writes > $node.out 2>&1" &
done

wait

end2=`date +%s`
echo "> $(( ($end2-$start2) ))s"

sleep 10

done
done
done
done
done

END

echo "END: `date +%H:%M:%S`"
end=`date +%s`
echo "Duration: $(( ($end-$start)/60 ))min"

:<<'END'
END

