nodes=(node1 node2 node3 node4 node5 node6 node7 node8)

for node in ${nodes[*]}; do
	ssh $node "pkill -u a36804 java"
done
