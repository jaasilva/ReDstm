#!/bin/bash
###############################################################################
# <replicas>
###############################################################################

#for _groups in 1 2 4 5 10 20; do
for _run in 1 2 3 4 5 6 7 8 9 10; do
	pbsdsh -- ./jsilva/trxsys/scripts/run/pbs/rbtX-full_rep.sh \
		$1 $_run
	#pbsdsh -- ./jsilva/trxsys/scripts/run/pbs/rbtX-par_rep.sh \
	#	$1 $_run $_groups
done
#done

