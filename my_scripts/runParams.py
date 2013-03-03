import sys
from subprocess import *

jagent = '-javaagent:'
deuceagent = 'bin/deuceAgent.jar'

initheapsize = "-Xms%sg"
maxheapsize = "-Xmx%sg"

cp = '-cp'
classpath = 'bin/classes:etc:lib/appia-core-4.1.2.jar:lib/appia-groupcomm-4.1.2.jar:	lib/appia-test-4.1.2.jar:lib/flanagan.jar:lib/jgcs-0.6.1.jar:lib/jgroups-3.1.0.Beta1.jar:lib/junit-4.6.jar:lib/jvstm.jar:lib/log4j-1.2.14.jar:lib/spread-4.2.0.jar'

exclude = '-Dorg.deuce.exclude='
excludes = 'java.*,sun.*,org.eclipse.*,org.junit.*,junit.*,net.sf.appia.*,net.sf.jgcs.*,org.jgroups.*,flanagan.*,org.apache.log4j.*,spread.*,org.deuce.trove.*'

contextClass = '-Dorg.deuce.transaction.contextClass='
commClass = '-Dtribu.groupcommunication.class='
commGroup = '-Dtribu.groupcommunication.group='
site = '-Dtribu.site='
replicas = '-Dtribu.replicas='
protocolClass = '-Dtribu.distributed.protocolClass='
compress = '-Dtribu.serialization.compress='
compressRes = 'true'

intset = 'org.deuce.benchmark.intset.Benchmark'
benchmarks = {
	'genome': 'jstamp.genome.Genome',
	'intruder': 'jstamp.intruder.Intruder',
	'vacation': 'jstamp.vacation.Vacation',
	'linkedlist': "%s LinkedList" % intset,
	'skiplist': "%s SkipList" % intset,
	'rbtree': "%s RBTree" % intset
}

def usage(argv):
	print "%s <benchmark> <comm> <site> <threads> <replicas> <run> <contextClass> <protocolClass> [...]" % argv[0]

def vacation(bench, comm, sitenum, threads, replicasnum, run, args, stmArgs):
	call = Popen(
		['java', initheapsize, maxheapsize, cp, classpath,
		jagent + deuceagent,
		contextClass + stmArgs[0],
		exclude + excludes,
		commClass + stmArgs[1],
		commGroup + "%s_%s" % (bench, run),
		site + sitenum,
		replicas + replicasnum,
		protocolClass + stmArgs[2],
		compress + compressRes,
		bench,
		'-c', threads, '-q', args[0], '-u', args[1],
		'-r', args[2], '-t', args[4]/replicasnum, '-n', args[3]
		], stdout=PIPE)
	print call.stdout.read()

def genome(bench, comm, sitenum, threads, replicasnum, run, args, stmArgs):
	call = Popen(
		['java', initheapsize, maxheapsize, cp, classpath,
		jagent + deuceagent,
		contextClass + stmArgs[0],
		exclude + excludes,
		commClass + stmArgs[1],
		commGroup + "%s_%s" % (bench, run),
		site + sitenum,
		replicas + replicasnum,
		protocolClass + stmArgs[2],
		compress + compressRes,
		bench,
		'-g', args[0], '-s', args[1], '-n', args[2], '-t', threads
		], stdout=PIPE)
	print call.stdout.read()

def intruder(bench, comm, sitenum, threads, replicasnum, run, args, stmArgs):
	call = Popen(
		['java', initheapsize, maxheapsize, cp, classpath,
		jagent + deuceagent,
		contextClass + stmArgs[1],
		exclude + excludes,
		commClass + stmArgs[2],
		commGroup + "%s_%s" % (bench, run),
		site + sitenum,
		replicas + replicasnum,
		protocolClass + stmArgs[2],
		compress + compressRes,
		bench,
		'-t', threads, '-a', args[0], '-l', args[1], '-n', args[2]
		], stdout=PIPE)
	print call.stdout.read()

def intset(bench, comm, sitenum, threads, replicasnum, run, args, stmArgs):
	benchsplit = bench.split(' ')
	bench1 = benchsplit[0]
	bench2 = benchsplit[1]
	call = Popen(['java', initheapsize, maxheapsize, cp, classpath,
		jagent + deuceagent,
		contextClass + stmArgs[0],
		exclude + excludes,
		commClass + stmArgs[1],
		commGroup + "%s_%s" % (bench.replace(' ', '_'), run),
		site + sitenum,
		replicas + replicasnum,
		protocolClass + stmArgs[2],
		compress + compressRes,
		'org.deuce.benchmark.Driver', '-n', threads, '-d', args[0],
		'-w', args[1],
		bench1, bench2,
		'-r', args[2], '-i', args[3], '-w', args[4]
		], stdout=PIPE)
	print call.stdout.read()

def main(argv):
	try:
		heapsz = argv[1]
		bench = argv[2]
		comm = argv[3]
		site = argv[4]
		threads = argv[5]
		replicas = argv[6]
		run = argv[7]
		
		initheapsize = initheapsize % heapsz
		maxheapsize = maxheapsize % heapsz
		contextClass = "org.deuce.transaction.%s" % argv[8]
		commClass = "org.deuce.distribution.groupcomm.%sGroupCommunication" % comm
		protocolClass = "org.deuce.distribution.replication.%s" % argv[9]
		stmArgs = (contextClass, commClass, protocolClass)
	except:
		usage(argv)
		sys.exit()
	
	if bench == 'genome':
		try:
			# <gene_length> <segment_length> <segments>
			args = (argv[10], argv[11], argv[12])
		except:
			print 'Missing benchmark specific arguments: <gene_length> <segment_length> <segments>'
			sys.exit(-1)
		genome(benchmarks[bench], comm, site, threads, replicas, run, args, stmArgs)
	elif bench == 'intruder':
		try:
			# <attacks> <packets> <flows>
			args = (argv[10], argv[11], argv[12])
		except:
			print 'Missing benchmark specific arguments: <attacks> <packets> <flows>'
			sys.exit(-1)
		intruder(benchmarks[bench], comm, site, threads, replicas, run, args, stmArgs)
	elif bench == 'vacation':
		try:
			# <perc_queried> <perc_reservation>
			# <relations> <tasks/replicas> <task_queries> <tasks>
			args = (argv[10], argv[11], argv[12], argv[13], argv[14], argv[15])
		except:
			print 'Missing benchmark specific arguments: <perc_queried> <perc_reservation> <relations> <tasks/replicas> <task_queries> <tasks>'
			sys.exit(-1)
		vacation(benchmarks[bench], comm, site, threads, replicas, run, args, stmArgs)
	elif bench == 'linkedlist' or bench == 'skiplist' or bench == 'rbtree':
		try:
			# <duration> <warmup> <range> <size> <writes>
			args = (argv[10], argv[11], argv[12], argv[13], argv[14])
		except:
			print 'Missing benchmark specific arguments: <duration> <warmup> <range> <size> <writes>'
			sys.exit(-1)
		intset(benchmarks[bench], comm, site, threads, replicas, run, args, stmArgs)

if __name__ == '__main__':
	main(sys.argv)
