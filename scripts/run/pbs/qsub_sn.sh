#!/bin/sh
###############################################################################
# <replicas>
###############################################################################

_bench=RedBTreeZ
_thrs=4
_reps=$1
_groups=$(( $_reps / 2 )) # rep. factor 2
#_groups=$2
_writes=10
#_writes=20
#_writes=50
#_writes=80
_runs=5
_pops=0
#_pops=10
#_pops=50
#_pops=80
#_pops=100
_mem=3GB
_model=2_Xeon_X5650_24GB

_jobId=""

######################################################
# short6h, short48h, short7d, normal, bigm, infinity
# -l place=scatter:excl
######################################################

for _run in `seq 1 $_runs`
do
	_jobId=$(cat <<EOF | qsub -
	#!/bin/sh
	#PBS -N ${_reps}-${_run}-${_groups}
	#PBS -l select=${_reps}:ncpus=${_thrs}:mem=${_mem}:hw_model=${_model}
	#PBS -l walltime=00:05:00
	#PBS -l place=scatter
	#PBS -q short6h
	#PBS -M jaa.silva@campus.fct.unl.pt
	#PBS -m abe
	#PBS -j oe
	#PBS -W depend=afterany:$jobId
	
	pbsdsh -- ./jsilva/trxsys/scripts/run/intset-full_rep.sh $_bench $_thrs \
		$_reps $_run $_writes $_pops
	
	echo "##### FINISHED!"
	EOF)
echo "> ${_jobId}"
done

