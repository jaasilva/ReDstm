#!/bin/sh

DIR=$1
benchmark=Intruder
attacks=10
packets=4
flows=2048
seed=1

for comm in appia.Appia jgroups.JGroups spread.Spread
do

	data="logs/${DIR}${benchmark}_a${attacks}_l${packets}_n${flows}_s${seed}_${comm}.data"
	data_brkdwn="logs/${DIR}${benchmark}_a${attacks}_l${packets}_n${flows}_s${seed}_${comm}_breakdown.data"
	data_aborts="logs/${DIR}${benchmark}_a${attacks}_l${packets}_n${flows}_s${seed}_${comm}_aborts.data"
	rm -f $data
	rm -f $data_brkdwn
	rm -f $data_aborts
	
	echo '"Replicas"	"1 thread"	"2 threads"	"4 threads"' >$data
	echo '"(Replicas, threads)"	"R1"	"R2"	"R3"	"R4"	"R5"	"R6"	"R7"	"R8"' >$data_brkdwn
	echo '"Replicas"	"1 thread"	"2 threads"	"4 threads"' >$data_aborts
	
	for sites in 2 4 6 8
	do
		echo -n "${sites}" >>$data
		echo -n "${sites}" >>$data_aborts
	
		for thread in 1 2 4
		do
			echo -n "\"(${sites}, ${thread})\"" >>$data_brkdwn
	
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
					log="logs/${benchmark}_a${attacks}_l${packets}_n${flows}_s${seed}_t${thread}_nonvoting.NonVoting_${comm}_id${site}-${sites}_run${run}.res"
					duration=`grep "TIME=" ${log} | awk 'BEGIN { FS="=" } { i = $2 } END { printf "%d", i/1000 }'`
					run_durations[$run]=`expr ${run_durations[$run]} + ${duration}`

					if [[ $run -eq 4 ]]
					then
						# -- BRKDWN --
						txs=`grep " Committed " ${log} | awk '{ a = $6 } END { printf "%d", a }'`
						run_txs[$run]=`expr ${run_txs[$run]} + ${txs}`

						latency[$site]=`grep -A 1 "TO" ${log} | grep "avg" | awk '{ l = $3 } END { printf "%d", l }'`
						trx[$site]=$txs

						# -- ABORTS --
						aborts=`grep " Aborted " ${log} | awk '{ a = $7 } END { printf "%d", a }'`
						aborts[$site]=$aborts
					fi
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

			# -- BRKDWN --
			sum_trx=0
			sum_aborts=0
			for (( i = 1; i <= $sites; i++ ))
			do
				sum_trx=`expr ${sum_trx} + ${trx[$i]}`
				# -- ABORTS --
				sum_aborts=`expr ${sum_aborts} + ${aborts[$i]}`

			done
			for (( i = 1; i <= $sites; i++ ))
			do
				site_perc=`echo "scale=10; ${trx[$i]} / ${sum_trx}" | bc`
				echo -n "	${site_perc}" >>$data_brkdwn
			done

			# -- ABORTS --
			echo "	commits: ${sum_trx}"
			echo "	aborts: ${sum_aborts}"
			aborts=`echo "scale=10; 100 * ${sum_aborts} / ${sum_trx}" | bc`
			echo "	abort rate: ${aborts}%"
			echo -n "	${aborts}" >>$data_aborts

			echo "" >>$data_brkdwn
		done
	
		echo "" >>$data
		echo "" >>$data_aborts
	done
	
	# -- PERF --
	pscript="logs/${DIR}graph_${benchmark}_a${attacks}_l${packets}_n${flows}_s${seed}_${comm}.p"
	eps="logs/${DIR}graph_${benchmark}_a${attacks}_l${packets}_n${flows}_s${seed}_${comm}.eps"
	
	title=(${comm//./ });
	echo "set autoscale" >$pscript
	echo "unset log" >>$pscript
	echo "unset label" >>$pscript
	echo "set boxwidth 0.9 relative" >>$pscript
	#echo "set style data linespoints" >>$pscript
	echo "set style fill solid 1.0 border lt -1" >>$pscript
	echo "set xtics 1" >>$pscript
	echo "set grid ytics" >>$pscript
	echo "set ytics 35" >>$pscript
	echo "set yrange [0:350]" >>$pscript
	#echo "set xrange [1:7]" >>$pscript
	echo "set title \"${title[1]}, ${benchmark}\"" >>$pscript
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

	# -- ABORTS --
	pscript="logs/${DIR}graph_${benchmark}_a${attacks}_l${packets}_n${flows}_s${seed}_${comm}_aborts.p"
	eps="logs/${DIR}graph_${benchmark}_a${attacks}_l${packets}_n${flows}_s${seed}_${comm}_aborts.eps"
	
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
	#echo "set yrange [4:19]" >>$pscript
	#echo "set xrange [1:7]" >>$pscript
	echo "set title \"${title[1]}, ${benchmark}\"" >>$pscript
	echo "set xlabel \"Replicas\"" >>$pscript
	echo "set ylabel \"Aborts (%)\"" >>$pscript
	echo "set key top left" >>$pscript
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
		echo "plot \"${data_aborts}\" using 1:2 with linespoints linestyle 1 title columnhead(2), \\" >>$pscript
		echo "'' using 1:3 with linespoints linestyle 2 title columnhead(3), \\" >>$pscript
		echo "'' using 1:4 with linespoints linestyle 3 title columnhead(4)" >>$pscript
	
	gnuplot $pscript
	epstopdf $eps

	# -- BRKDWN --
	pscript="logs/${DIR}graph_${benchmark}_a${attacks}_l${packets}_n${flows}_s${seed}_${comm}_breakdown.p"
	eps="logs/${DIR}graph_${benchmark}_a${attacks}_l${packets}_n${flows}_s${seed}_${comm}_breakdown.eps"

	echo "set autoscale" >$pscript
	echo "unset log" >>$pscript
	echo "unset label" >>$pscript
	echo "set boxwidth 0.9 relative" >>$pscript
	echo "set style data histograms" >>$pscript
	echo "set style histogram rowstacked gap 8" >>$pscript
	echo "set style fill solid 1.0 border lt -1" >>$pscript
	echo "set xtics border in scale 0,0 nomirror rotate by -45  offset character 0, 0, 0 autojustify" >>$pscript
	echo "set xtics norangelimit font \",16\"" >>$pscript
	echo "set ytic auto" >>$pscript
	echo "set title \"${title[1]}, ${benchmark}\"" >>$pscript
	echo "set xlabel \"(Replicas, threads)\"" >>$pscript
	echo "set ylabel \"Transactions (% from total)\"" >>$pscript
	#echo "set key outside right" >>$pscript
	echo "unset key" >>$pscript
	echo "set term postscript eps enhanced color 22" >>$pscript
	echo "set output \"${eps}\"" >>$pscript
	echo "set yrange [0:100]" >>$pscript
	echo "plot for [i=2:9] \"${data_brkdwn}\" using (100.*column(i)):xticlabels(1) title columnhead(i)" >>$pscript

	gnuplot $pscript
	epstopdf $eps

done	
