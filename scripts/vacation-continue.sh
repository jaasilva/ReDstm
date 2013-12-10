for comm in jgroups.JGroups #appia.Appia spread.Spread
do
for threads in 4
do
for proto in score.SCOReProtocol
do
for _data in Simple RoundRobin Random
do
for _uc in 90 0
do
for _groups in 1 2 4 8
do
for run in 1 2 3 4 5
do

echo "###########################"
echo "Benchmark: $bench, run $run"
echo "Thrs: $threads, Rpls: 8, Groups: $_groups"
echo "Comm: $comm, read-only: $_uc%"
echo "DPart: $_data"
echo "Protocol: $proto"
echo "Time: `date +%H:%M:%S`"
echo "###########################"

start2=`date +%s`

for node in node1 node2 node3 node4 node5 node6 node7 node8
do
	ssh $node "cd ./repos/metadata; ./scripts/tribu-vacation_score.sh $threads 8 $_groups $run $_data $_uc $proto > $node.out 2>&1" &
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

# ---------------------------------------------------------

for comm in jgroups.JGroups #appia.Appia spread.Spread
do
for threads in 4
do
for proto in score.SCOReProtocol_noReadOpt
do
for _data in Simple
do
for _uc in 90
do
for _groups in 1 2 4 8
do
for run in 1 2 3 4 5
do

echo "###########################"
echo "Benchmark: $bench, run $run"
echo "Thrs: $threads, Rpls: 8, Groups: $_groups"
echo "Comm: $comm, read-only: $_uc%"
echo "DPart: $_data"
echo "Protocol: $proto"
echo "Time: `date +%H:%M:%S`"
echo "###########################"

start2=`date +%s`

for node in node1 node2 node3 node4 node5 node6 node7 node8
do
	ssh $node "cd ./repos/metadata; ./scripts/tribu-vacation_score.sh $threads 8 $_groups $run $_data $_uc $proto > $node.out 2>&1" &
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

