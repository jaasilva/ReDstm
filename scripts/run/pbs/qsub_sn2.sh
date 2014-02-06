#!/bin/sh
###############################################################################
# <replicas>
###############################################################################

_bench=RedBTreeZ
_thrs=4
_reps=$1
_groups=$(( $_reps / 2 )) # rep. factor 2
#_groups=$2
_runs=5
_mem=3GB
_model=2_Xeon_X5650_24GB
_proto=nonvoting
#_proto=score
#_proto=score-c

_jobId=""

######################################################
# short6h, short48h, short7d, normal, bigm, infinity
# -l place=scatter:excl
######################################################

_jobId=$(cat <<EOF | qsub -
#!/bin/sh
#PBS -N rbtZ-full-n${_reps}
#PBS -l select=${_reps}:ncpus=${_thrs}:mem=${_mem}:hw_model=${_model}
#PBS -l place=scatter
#PBS -q normal
#PBS -M jaa.silva@campus.fct.unl.pt
#PBS -m abe
#PBS -j oe
#PBS -W depend=afterany:${_jobId}

for _writes in 10; do
#for _writes in 50; do
for _p_ops in 0 10 50 80 100; do	
	for _run in `seq 1 $_runs`; do
		
		pbsdsh -- ./jsilva/trxsys/scripts/run/pbs/intset-full_rep.sh \
			$_bench $_thrs $_reps $_run $_writes $_p_ops
		
	done
done
done

echo "##### FINISHED!"
EOF)

echo "> ${_jobId}"

