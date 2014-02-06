#!/bin/bash
###############################################################################
# <benchmark> <threads> <replicas>
###############################################################################

for _writes in 10; do
#for _writes in 50; do
for _pops in 0 10 50 80 100; do
for _run in 1 2 3 4 5 6 7 8 9 10; do
	pbsdsh -- ./jsilva/trxsys/scripts/run/pbs/intset-full_rep.sh \
		$1 $2 $3 $_run $_writes $_pops
done
done
done

