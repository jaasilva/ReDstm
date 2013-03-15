for node in node1 node2 node3 node4 node5 node6 node7 node8
do
	ssh $node cd ./repos/trxsys-tvale | java -cp .:bin/:bin/classes:etc:lib/jgroups-3.2.7.Final.jar:lib/log4j-1.2.14.jar -Dtribu.replicas=10 org.deuce.partial.toa.perf.Receiver $node > /dev/null &
done
