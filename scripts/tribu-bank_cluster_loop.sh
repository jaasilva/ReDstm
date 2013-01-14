#!/bin/sh

site=`hostname | cut -c 5-` # nodeX -> X
sites=$1

for comm in appia.Appia jgroups.JGroups spread.Spread; do
for disjoint in "" "-d"; do
for run in 2 3 4 5; do
for thread in 1 2 4; do

	scripts/tribu-bank_cluster.sh $disjoint $comm $site $thread $sites $run

done
done
done

