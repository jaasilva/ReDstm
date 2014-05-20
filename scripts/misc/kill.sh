nodes=(node2-vm1 node2-vm2 node2-vm3 node2-vm4 node3-vm1 node3-vm2 node3-vm3 node3-vm4 node4-vm1 node4-vm2 node4-vm3 node4-vm4 node5-vm1 node5-vm2 node5-vm3 node5-vm4 node6-vm1 node6-vm2 node6-vm3 node6-vm4 node7-vm1 node7-vm2 node7-vm3 node7-vm4 node8-vm1 node8-vm2 node8-vm3 node8-vm4 node9-vm1 node9-vm2 node9-vm3 node9-vm4 node10-vm1 node10-vm2 node10-vm3 node10-vm4 node11-vm1  node11-vm2 node11-vm3 node11-vm4 node12-vm1 node12-vm2 node12-vm3 node12-vm4 node15-vm1 node15-vm2 node15-vm3 node15-vm4 node16-vm1 node16-vm2 node16-vm3 node16-vm4 node17-vm1 node17-vm2 node17-vm3 node17-vm4 node18-vm1 node18-vm2 node18-vm3 node18-vm4)

for node in ${nodes[*]}; do
	#ssh $node 'kill -9 `ps -U a36804 | grep java | awk "{print $1}"`'
	ssh $node "pkill -u joaosilva java"
done
