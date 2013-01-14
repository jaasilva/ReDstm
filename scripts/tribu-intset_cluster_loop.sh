#!/bin/sh

impl=$1
site=`hostname | cut -c 5-` # nodeX -> X
sites=$2

for comm in appia.Appia jgroups.JGroups spread.Spread; do
for run in 2 3 4 5; do
for thread in 1 2 4; do

	scripts/tribu-intset_cluster.sh $impl $comm $site $thread $sites $run

done
done
done

