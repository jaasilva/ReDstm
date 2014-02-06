#!/bin/bash
###############################################################################
# <replicas>
###############################################################################

_bench=RedBTreeZ
_thrs=4
_reps=$1
_groups=$(( $_reps / 2 )) # rep. factor 2
_mem=3GB
_model=2_Xeon_X5650_24GB

_jobId=""

######################################################
# short6h, short48h, short7d, normal, bigm, infinity
# -l place=scatter:excl
######################################################

_jobId=$(cat <<EOF | qsub -
#!/bin/bash
#PBS -N rbtZ-f-n${_reps}
#PBS -l select=${_reps}:ncpus=${_thrs}:mem=${_mem}:hw_model=${_model}
#PBS -l place=scatter
#PBS -q normal
#PBS -M jaa.silva@campus.fct.unl.pt
#PBS -m abe
#PBS -j oe
#PBS -W depend=afterany:${_jobId}

./jsilva/trxsys/scripts/run/pbs/run.sh $_bench $_thrs $_reps

echo "##### FINISHED!"
EOF)

echo "> ${_jobId}"

