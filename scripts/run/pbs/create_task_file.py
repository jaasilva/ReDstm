###############################################################################
# <tasks_filename> <replicas> <app and args>
###############################################################################
import sys

filename = sys.argv[1]
nodes = int(sys.argv[2])
app_w_args = sys.argv[3:len(sys.argv)]
app_name = ' '.join(app_w_args)

file = open(filename, 'w')
for n in xrange(0, nodes):
	file.write('%s\n' % app_name)
	file.write('./scripts/run/pbs/sleep.sh\n'*3)

