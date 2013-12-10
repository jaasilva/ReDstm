echo "START: `date +%H:%M:%S`"
start=`date +%s`
bench=Vacation


############################################################################
echo "-----------------------------------------------------------"

for comm in jgroups.JGroups #appia.Appia spread.Spread
do
for threads in 4
do
for _uc in 90
do
for run in 1 2 3 4 5
do

echo "###########################"
echo "Benchmark: $bench, run $run"
echo "Thrs: $threads, Rpls: 8"
echo "Comm: $comm, read-only: $_uc%"
echo "Time: `date +%H:%M:%S`"
echo "###########################"

start2=`date +%s`

for node in node1 node2 node3 node4 node5 node6 node7 node8
do
	ssh $node "cd ./repos/metadata; ./scripts/tribu-vacation_cluster.sh $threads 8 $run $_uc > $node.out 2>&1" &
done

wait

end2=`date +%s`
echo "> $(( ($end2-$start2) ))s"

sleep 10

done
done
done
done

echo "END: `date +%H:%M:%S`"
end=`date +%s`
echo "Duration: $(( ($end-$start)/60 ))min"

