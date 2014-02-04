#!/bin/sh
###############################################################################
# <replicas>
###############################################################################

_bench=RedBTreeZ
_thrs=4 # has to be 4
_reps=$1
_groups=$(( $_reps / 2 ))
_writes=10
_runs=10
_pops=10
_proto=nonvoting

_jobId=""

for _run in `seq 1 $_runs`
do

	_jobId=$(cat <<EOF | qsub -
	
	#!/bin/bash
	#PBS -N ${_reps}-${_run}-${_groups}
	#PBS -S /bin/bash
	#PBS -A pdr_14
	#PBS -l nodes=${_reps}:ppn=${_thrs}
	#PBS -l walltime=00:05:00
	#PBS -q generic
	#PBS -M jaa.silva@campus.fct.unl.pt
	#PBS -m abe
	#PBS -j oe
	#PBS -o ./logs/${_bench}_r${_reps}_t${_thrs}_w${_writes}_g${_groups}_${_run}_${_proto}.res
	#PBS -W depend=afterany:$jobId
	
	module load comp/mpich2/intel64
	module load taskfarm
	
	cd $PBS_O_WORKDIR
	
	python ./scripts/run/pbs/create_task_file.py tasks $_reps ./scripts/run/intset-full_rep.sh $_bench $_thrs $_reps $_run $_writes $_pops
	#python ./scripts/run/pbs/create_task_file.py tasks $_reps ./scripts/run/intset-par_rep.sh $_bench $_thrs $_reps $_run $_writes $_groups $_pops
	
	mpipbsexec nop.sh
	taskfarm tasks
	
	echo "##### FINISHED!"
	EOF)

echo "> ${_jobId}"
done

