import sys
from subprocess import *

heapSize = 1 # GB
commClass = 'appia.Appia'
contextClass = 'org.deuce.distribution.transaction.tl2.Context'
protocol = 'org.deuce.distribution.replication.full.protocol.nonvoting.NonVoting'

def usage(argv):
	print "%s <benchmark> <site> <threads> <replicas> <run> [...]" % argv[0]

def main(argv):
	try:
		bench = argv[1]
		site = argv[2]
		threads = argv[3]
		replicas = argv[4]
		run = argv[5]
	except:
		usage(argv)
		sys.exit(-1)
	
	if bench == 'genome':
		try:
			params = argv[-1]
		except:
			print 'Missing benchmark specific argument: <benchType> <segment_length> <segments>'
			sys.exit(-1)
			
	elif bench == 'intruder':
		
	elif bench == 'vacation':
		
	elif bench == 'linkedlist' or bench == 'skiplist' or bench == 'rbtree':
		

if __name__ == '__main__':
	main(sys.argv)
