nodes=(node1 node2 node3 node4 node5 node6 node7 node8)

for node in ${nodes[*]}; do
	#ssh $node 'kill -9 `ps -U a36804 | grep java | awk "{print $1}"`'
	ssh $node "pkill -u a36804 java; pkill -u a36804 python"
done
