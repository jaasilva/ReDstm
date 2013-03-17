
for size in 64 128 256 512 1024 8192 16384 32768 65536 131072 262144 524288 1048576
do
	for threads in 1 2 4 6 8
	do
		echo "-----Deploying $threads threads using msg_size=$size"
		echo "  Deployed in"
		for node in node1 node2 node3 node4 node5 node6 node7 node8
		do
			echo "          $node"
			ssh $node "cd ./repos/trxsys-tvale; java -cp .:bin/:bin/classes:etc:lib/jgroups-3.2.7.Final.jar:lib/log4j-1.2.14.jar -Dtribu.groupcommunication.group=jaasilva-$size-$threads -Dtribu.replicas=$((8*$threads)) org.deuce.partial.toa.perf.Receiver $node-$size-$threads $size $threads > /dev/null" & echo $! > $node.pid
		done
		wait $(<node1.pid) $(<node2.pid) $(<node3.pid) $(<node4.pid) $(<node5.pid) $(<node6.pid) $(<node7.pid) $(<node8.pid)
	done
done
