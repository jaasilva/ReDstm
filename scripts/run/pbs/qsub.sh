#!/bin/bash
###############################################################################
# <replicas>
###############################################################################

_bench=RedBTreeZ
_thrs=4 # has to be 4
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
_proto=nonvoting
#_proto=score

_jobId=""

for _run in `seq 1 $_runs`
do
	_jobId=$(cat <<EOF | qsub - #!/bin/bash
	#PBS -S /bin/bash
	#PBS -A pdr_14
	#PBS -N ${_reps}-${_run}-${_groups}
	#PBS -l walltime=00:05:00
	#PBS -l nodes=${_reps}:ppn=${_thrs}
	#PBS -q generic
	#PBS -m abe -M jaa.silva@campus.fct.unl.pt
	#PBS -j oe
	#PBS -W depend=afterany:$jobId
	
	module load comp/mpich2/intel64
	module load taskfarm
	
	cd $PBS_O_WORKDIR
	
	python ./scripts/run/pbs/create_task_file.py \
		tasks-${_reps}-${_run}-${_groups} $_reps \
		./scripts/run/intset-full_rep.sh $_bench $_thrs \
		$_reps $_run $_writes $_pops
	#python ./scripts/run/pbs/create_task_file.py \
	#	tasks-${_reps}-${_run}-${_groups} $_reps \
	#	./scripts/run/intset-par_rep.sh $_bench $_thrs \
	#	$_reps $_run $_writes $_groups $_pops
	
	mpipbsexec ./scripts/run/pbs/nop.sh
	taskfarm tasks-${_reps}-${_run}-${_groups}
	
	echo "##### FINISHED!"
	EOF)
echo "> ${_jobId}"
done

