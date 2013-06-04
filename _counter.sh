echo "START: `date +%H:%M:%S`"
count=0

while true
do
	#for threads in {1..2}
	#do
		
		echo "$count - Deploying..."
		for node in 1 2 3
		do
			./counter_prep.sh Counter $node 1 3 1 $node > $node.out 2>&1 & echo $! > $node.pid
			# ./counter_prep <bench> <site> <threads> <replicas> <run> <log>
		
		done
		
		echo "Waiting..."
		count=$((count + 1))
		
		wait $(<1.pid) $(<2.pid) $(<3.pid) #$(<4.pid)
		
		echo "Sleeping..."
		sleep 5
		echo "----------------------------"
	
	#done
done

