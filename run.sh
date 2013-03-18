#size_=(32 64 128 256 512 1024 2048 4096 8192 16384 32768 65536 131072 262144 524288 1048576)
size_=(1024 2048 4096)
#msgs_=(1000 5000 10000 50000 100000)
msgs_=(10000 50000)
threads_=(1 2 4 8)

: <<'END'
echo "USING 1 NODE"
nodes=(node1)

for size in ${size_[*]}
do

	echo "Using msg_size=$size"
	for msgs in ${msgs_[*]}
	do

		echo "  Using n_msgs=$msgs"
		for threads in ${threads_[*]}
		do
	
			echo "    Deploying $threads threads"
			echo "        Deployed in"
			for node in ${nodes[*]}
			do
		
				echo "            $node"
				ssh $node "cd ./repos/trxsys-tvale; java -cp .:bin/:bin/classes:etc:lib/jgroups-3.2.7.Final.jar:lib/log4j-1.2.14.jar -Dtribu.groupcommunication.group=jaasilva-$size-$threads -Dtribu.replicas=$((${#nodes[*]}*$threads)) org.deuce.partial.toa.perf.Receiver $node-$size-$threads-$msgs-1 $size $threads $msgs > /dev/null" & echo $! > $node.pid
		
			done
			wait $(<node1.pid)
			rm log.out node*
	
		done
	
	done

done

###########################################################
echo "###############################################"
echo "USING 2 NODES"
nodes=(node1 node2)

for size in ${size_[*]}
do

	echo "Using msg_size=$size"
	for msgs in ${msgs_[*]}
	do

		echo "  Using n_msgs=$msgs"
		for threads in ${threads_[*]}
		do
	
			echo "    Deploying $threads threads"
			echo "        Deployed in"
			for node in ${nodes[*]}
			do
		
				echo "            $node"
				ssh $node "cd ./repos/trxsys-tvale; java -cp .:bin/:bin/classes:etc:lib/jgroups-3.2.7.Final.jar:lib/log4j-1.2.14.jar -Dtribu.groupcommunication.group=jaasilva-$size-$threads -Dtribu.replicas=$((${#nodes[*]}*$threads)) org.deuce.partial.toa.perf.Receiver $node-$size-$threads-$msgs-2 $size $threads $msgs > /dev/null" & echo $! > $node.pid
		
			done
			wait $(<node1.pid) $(<node2.pid)
			rm log.out node*
	
		done
	
	done

done


###########################################################
echo "###############################################"
echo "USING 4 NODES"
nodes=(node1 node2 node3 node4)

for size in ${size_[*]}
do

	echo "Using msg_size=$size"
	for msgs in ${msgs_[*]}
	do

		echo "  Using n_msgs=$msgs"
		for threads in ${threads_[*]}
		do
	
			echo "    Deploying $threads threads"
			echo "        Deployed in"
			for node in ${nodes[*]}
			do
		
				echo "            $node"
				ssh $node "cd ./repos/trxsys-tvale; java -cp .:bin/:bin/classes:etc:lib/jgroups-3.2.7.Final.jar:lib/log4j-1.2.14.jar -Dtribu.groupcommunication.group=jaasilva-$size-$threads -Dtribu.replicas=$((${#nodes[*]}*$threads)) org.deuce.partial.toa.perf.Receiver $node-$size-$threads-$msgs-4 $size $threads $msgs > /dev/null" & echo $! > $node.pid
		
			done
			wait $(<node1.pid) $(<node2.pid) $(<node3.pid) $(<node4.pid)
			rm log.out node*
	
		done
	
	done

done
END

###########################################################
echo "###############################################"
echo "USING 8 NODES"
nodes=(node1 node2 node3 node4 node5 node6 node7 node8)

for size in ${size_[*]}
do

	echo "Using msg_size=$size"
	for msgs in ${msgs_[*]}
	do

		echo "  Using n_msgs=$msgs"
		for threads in ${threads_[*]}
		do
	
			echo "    Deploying $threads threads"
			echo "        Deployed in"
			for node in ${nodes[*]}
			do
		
				echo "            $node"
				ssh $node "cd ./repos/trxsys-tvale; java -cp .:bin/:bin/classes:etc:lib/jgroups-3.2.7.Final.jar:lib/log4j-1.2.14.jar -Dtribu.groupcommunication.group=jaasilva-$size-$threads -Dtribu.replicas=$((${#nodes[*]}*$threads)) org.deuce.partial.toa.perf.Receiver $node-$size-$threads-$msgs-8 $size $threads $msgs > /dev/null" & echo $! > $node.pid
		
			done
			wait $(<node1.pid) $(<node2.pid) $(<node3.pid) $(<node4.pid) $(<node5.pid) $(<node6.pid) $(<node7.pid) $(<node8.pid)
			rm log.out node*
	
		done
	
	done

done

