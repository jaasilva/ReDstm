###############################################################################
# <tasks_filename> <replicas> <app and args>
###############################################################################
import sys

def main (argv):
	filename = argv[1]
	nodes = int(argv[2])
	app_w_args = argv[3:len(argv)]
	app_name = ' '.join(app_w_args)

	file = open(filename, 'w')
	for n in xrange(0, nodes):
		file.write('%s\n' % app_name)
		file.write('./scripts/run/pbs/sleep.sh\n')
		file.write('./scripts/run/pbs/sleep.sh\n')
		file.write('./scripts/run/pbs/sleep.sh\n')

if __name__ == '__main__':
	main(sys.argv)

