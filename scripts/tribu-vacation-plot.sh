#!/bin/sh

DIR=$1
benchmark=Vacation

task_queries=2
perc_queries=90
perc_reservation=98
relations=16384
tasks=4096

for comm in appia.Appia jgroups.JGroups spread.Spread
do

	data="logs/${DIR}${benchmark}_n${task_queries}_q${perc_queries}_u${perc_reservation}_r${relations}_t${tasks}_${comm}.data"
	rm -f $data
	
	echo '"Replicas"	"1 thread"	"2 threads"	"4 threads"' >${data}
	
	for sites in 2 4 6 8
	do
		echo -n "${sites}" >>${data}
	
		for thread in 1 2 4
		do
	
			elapsed=0
			run_durations=()
			max=1
			min=1
			# -- 1 RUN --
				#runs=1
			# -- 5 RUNS --
				runs=5
	
			for (( run = 1; run <= $runs; run++ ))
			do
				run_durations[$run]=0
				ok=0
				for (( site = 1; site <= $sites; site++ ))
				do
					# -- PERF --
					log="logs/${benchmark}_n${task_queries}_q${perc_queries}_u${perc_reservation}_r${relations}_t${tasks}_t${thread}_nonvoting.NonVoting_${comm}_id${site}-${sites}_run${run}.res"
					duration=`grep "TIME2=" ${log} | awk 'BEGIN { FS="=" } { i = $2 } END { printf "%d", i/1000 }'`
					run_durations[$run]=`expr ${run_durations[$run]} + ${duration}`
				done
				run_durations[$run]=`expr ${run_durations[$run]} / ${sites}`
	
				# -- 5 RUNS --
				if [[ ${run_durations[$run]} -gt ${run_durations[$max]} ]]
				then
					max=$run
				fi
				if [[ ${run_durations[$run]} -lt ${run_durations[$min]} ]]
				then
					min=$run
				fi
			done
	
			echo "replicas=${sites}, th=${thread}"
			echo "	max: ${run_durations[$max]}"
			echo "	min: ${run_durations[$min]}"
	
			elapsed=0
			for (( i = 1; i <= $runs; i++ ))
			do
				# -- 1 RUN --
					#elapsed=`expr ${elapsed} + ${run_durations[$i]}`
				# -- 5 RUN --
					if [[ $i -ne $min && $i -ne $max ]]
					then
						elapsed=`expr ${elapsed} + ${run_durations[$i]}`
					fi
			done
	
			# -- PERF --
			# -- 1 RUN
				#rs=${runs}
			# -- 5 RUNS --
				if [[ $max -eq $min ]]
				then
					rs=`expr ${runs} - 1`
				else
					rs=`expr ${runs} - 2`
				fi
			echo "	runs: ${rs}"
			elapsed=`expr ${elapsed} / ${rs}`
			echo "	elapsed: ${elapsed}"
			# -- WITH DEV --
				#echo -n "	${elapsed}	${elapsed_min}	${elapsed_max}" >>${data}
			# -- WITHOUT DEV --
				echo -n "	${elapsed}" >>$data
		done
	
		echo "" >>$data
	done
	
	# -- PERF --
	pscript="logs/${DIR}graph_${benchmark}_n${task_queries}_q${perc_queries}_u${perc_reservation}_r${relations}_t${tasks}_${comm}.p"
	eps="logs/${DIR}graph_${benchmark}_n${task_queries}_q${perc_queries}_u${perc_reservation}_r${relations}_t${tasks}_${comm}.eps"
	
	title=(${comm//./ });
	echo "set autoscale" >$pscript
	echo "unset log" >>$pscript
	echo "unset label" >>$pscript
	echo "set boxwidth 0.9 relative" >>$pscript
	#echo "set style data linespoints" >>$pscript
	echo "set style fill solid 1.0 border lt -1" >>$pscript
	echo "set xtics 1" >>$pscript
	echo "set ytic auto" >>$pscript
	echo "set grid ytics" >>$pscript
	echo "set yrange [0:90]" >>$pscript
	#echo "set xrange [1:7]" >>$pscript
	echo "set title \"${title[1]}, ${benchmark} (low)\"" >>$pscript
	echo "set xlabel \"Replicas\"" >>$pscript
	echo "set ylabel \"Execution time (s)\"" >>$pscript
	echo "set key top right" >>$pscript
	echo "set term postscript eps enhanced color 22" >>$pscript
	echo "set output \"${eps}\"" >>$pscript
	echo "set style line 1 linetype 1 pointtype 1 linewidth 3" >>$pscript
	echo "set style line 2 linetype 2 pointtype 2 linewidth 3" >>$pscript
	echo "set style line 3 linetype 4 pointtype 4 linewidth 3" >>$pscript
	# -- WITH DEV --
		#echo "plot \"${data}\" using 1:2:3:4 with yerrorlines title columnhead(2), \\" >>$pscript
		#echo "'' using 1:5:6:7 with yerrorlines title columnhead(3), \\" >>$pscript
		#echo "'' using 1:8:9:10 with yerrorlines title columnhead(4)" >>$pscript
	# -- WITHOUT DEV --
		echo "plot \"${data}\" using 1:2 with linespoints linestyle 1 title columnhead(2), \\" >>$pscript
		echo "'' using 1:3 with linespoints linestyle 2 title columnhead(3), \\" >>$pscript
		echo "'' using 1:4 with linespoints linestyle 3 title columnhead(4)" >>$pscript
	
	gnuplot $pscript
	epstopdf $eps
	
done
