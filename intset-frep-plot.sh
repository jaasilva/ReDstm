#!/bin/bash

DIR=$1
BENCH=RBTree
SIZE=32768
WRITE=10
COMM=JGroups
RUNS=10
DATA="${DIR}/${BENCH}_${COMM}.data"

rm -f $DATA
echo '"Replicas"	"1 thread"	"2 threads"	"4 threads"	"6 threads"	"8 threads"' >$DATA

for replicas in 2 4 6 8 #10 20 30 40 50 60 70 80 90 100
do
	echo -n "${replicas}" >>$DATA
	
	for threads in 1 2 4 6 8
	do
		duration=0
		run_its=()
		max=1
		min=1
		
		for run in `seq $RUNS`
		do
			log="${DIR}/${BENCH}_r${replicas}_t${threads}_run${run}_${COMM}.o"
			its=`grep "Nb iterations" ${log} | awk '{s+=$4} END {printf "%d", s}'`
			duration=`grep "Test duration" ${log} | awk '{d=$5} END {printf "%d", d/1000}'`
			run_its[$run]=$its
			
			if [[ ${run_its[$run]} -gt ${run_its[$max]} ]]
			then
				max=$run
			fi
			if [[ ${run_its[$run]} -lt ${run_its[$min]} ]]
			then
				min=$run
			fi
		done
		
		echo "replicas:${replicas}, threads:${threads}"
		echo "  max: ${run_its[$max]}"
		echo "  min: ${run_its[$min]}"
		
		sum_its=0
		for i in `seq $RUNS`
		do
			if [[ $i -ne $min && $i -ne $max ]]
			then
				sum_its=`expr ${sum_its} + ${run_its[$i]}`
			fi
		done
		
		echo "  sum: ${sum_its}"
		
		rs=$RUNS
		if [[ $max -eq $min ]]
		then
			rs=`expr ${rs} - 1`
		else
			rs=`expr ${rs} - 2`
		fi
		
		echo "  runs: ${rs}"
		
		avg_runs=`expr ${sum_its} / ${rs}`
		
		echo "  avg: ${avg_runs}"
		echo "  dur: ${duration}s"
		
		txps=`expr ${avg_runs} / ${duration}`
		
		echo "  txps: ${txps}"
		echo -n "	${txps}" >>$DATA
	done

	echo "" >>$DATA
done

pscript="${DIR}/${BENCH}_${COMM}.p"
eps="${DIR}/${BENCH}_${COMM}.eps"

echo "set autoscale" >$pscript
echo "unset log" >>$pscript
echo "unset label" >>$pscript

echo "set xtics 1" >>$pscript
echo "set grid ytics" >>$pscript
echo "set ytics" >>$pscript
echo "set yrange [0:]" >>$pscript

echo "set title \"${COMM}, ${BENCH} (size=${SIZE}, update=${WRITE}%)\"" >>$pscript
echo "set xlabel \"Replicas\"" >>$pscript
echo "set ylabel \"Transactions/second\"" >>$pscript
echo "set key bottom right" >>$pscript

echo "set term postscript eps enhanced color 22" >>$pscript
echo "set output \"${eps}\"" >>$pscript

echo "plot \"${DATA}\" using 1:2 with linespoints title columnhead(2), \\" >>$pscript
echo "'' using 1:3 with linespoints title columnhead(3), \\" >>$pscript
echo "'' using 1:4 with linespoints title columnhead(4), \\" >>$pscript
echo "'' using 1:5 with linespoints title columnhead(5)" >>$pscript

gnuplot $pscript
epstopdf $eps

