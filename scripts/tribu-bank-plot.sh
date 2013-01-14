#!/bin/sh

DIR=$1
benchmark=Bank
read_all=0
write_all=0
disjoint="-d"
for comm in appia.Appia jgroups.JGroups spread.Spread
do

	data="logs/${DIR}${benchmark}_r${read_all}_w${write_all}_${disjoint}_${comm}.data"
	data_brkdwn="logs/${DIR}${benchmark}_r${read_all}_w${write_all}_${disjoint}_${comm}_breakdown.data"
	data_aborts="logs/${DIR}${benchmark}_r${read_all}_w${write_all}_${disjoint}_${comm}_aborts.data"
	rm -f $data
	rm -f $data_brkdwn
	
	echo '"Replicas"	"1 thread"	"2 threads"	"4 threads"' >$data
	echo '"(Replicas, threads)"	"R1"	"R2"	"R3"	"R4"	"R5"	"R6"	"R7"	"R8"' >$data_brkdwn
	echo '"Replicas"	"1 thread"	"2 threads"	"4 threads"' >$data_aborts
	
	for sites in 2 4 6 8
	do
	
		echo -n "${sites}" >>$data
		echo -n "${sites}" >>$data_aborts

		#for thread in 1 2 4
		for thread in 1
		do
			accounts=`expr ${sites} \* 2`
			echo -n "\"(${sites}, ${thread})\"" >>$data_brkdwn
	
			sum_iterations=0
			run_iterations=()
			duration=0
			max=1
			min=1
			# -- 1 RUN --
				#runs=1
			# -- 5 RUNS --
				runs=5
	
			for (( run = 1; run <= $runs; run++ ))
			do
				run_iterations[$run]=0
				run_aborts[$run]=0
				run_duration[$run]=0
				ok=0
				for (( site = 1; site <= $sites; site++ ))
				do
					# -- PERF --
					log="logs/${DIR}${benchmark}_n${accounts}_r${read_all}_w${write_all}_${disjoint}_t${thread}_nonvoting.NonVoting_${comm}_id${site}-${sites}_run${run}.res"
					iterations=`grep "Nb iterations" ${log} | awk '{ i = $4 } END { printf "%d", i }'`
			        duration=`grep "Test duration" ${log} | awk '{ d = $5 } END { printf "%d", d/1000 }'`
					run_iterations[$run]=`expr ${run_iterations[$run]} + ${iterations}`
	
					# -- BRKDWN --
					latency[$site]=`grep -A 1 "TO" ${log} | grep "avg" | awk '{ l = $3 } END { printf "%d", l }'`
					trx[$site]=$iterations

					# -- ABORTS --
					aborts=`grep " Aborted " ${log} | awk '{ a = $7 } END { printf "%d", a }'`
					run_aborts[$run]=`expr ${run_aborts[$run]} + ${aborts}`
				done
	
				# -- 5 RUNS --
				if [[ ${run_iterations[$run]} -gt ${run_iterations[$max]} ]]
				then
					max=$run
				fi
				if [[ ${run_iterations[$run]} -lt ${run_iterations[$min]} ]]
				then
					min=$run
				fi
			done
	
			echo "replicas=${sites}, th=${thread}"
			echo "	max: ${run_iterations[$max]}"
			echo "	min: ${run_iterations[$min]}"
	
			sum_iterations=0
			sum_aborts=0
			for (( i = 1; i <= $runs; i++ ))
			do
				# -- 1 RUN --
					#sum_iterations=`expr ${sum_iterations} + ${run_iterations[$i]}`
					# -- ABORTS --
					#sum_aborts=`expr ${sum_aborts} + ${run_aborts[$i]}`
				# -- 5 RUNS --
					if [[ $i -ne $min && $i -ne $max ]]
					then
						brkdwn_run=$i
						sum_iterations=`expr ${sum_iterations} + ${run_iterations[$i]}`
						# -- ABORTS --
						sum_aborts=`expr ${sum_aborts} + ${run_aborts[$i]}`
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
			sum_iterations=`expr ${sum_iterations} / ${rs}`
			echo "	iterations: ${sum_iterations}"
			txps=`expr ${sum_iterations} / ${duration}`
			# -- ABORTS --
			sum_aborts=`expr ${sum_aborts} / ${rs}`
			echo "	aborts: ${sum_aborts}"
			aborts=`echo "scale=10; 100 * ${sum_aborts} / ${sum_iterations}" | bc`
			# -- 5 RUNS --
				txps_max=`expr ${run_iterations[$max]} / ${duration}`
				txps_min=`expr ${run_iterations[$min]} / ${duration}`
			echo "	txps: ${txps}"
			echo "	abort rate: ${aborts}%"
			# -- WITH DEV (needs 5 RUNS) --
				#echo -n "	${txps}	${txps_min}	${txps_max}" >>${data}
			# -- WITHOUT DEV --
				echo -n "	${txps}" >>$data
			echo -n "	${aborts}" >>$data_aborts
	
			# -- BRKDWN --
			sum_trx=0
			for i in $(seq 1 ${sites})
			do
				sum_trx=`expr ${sum_trx} + ${trx[$i]}`
			done
			for i in $(seq 1 ${sites})
			do
				site_perc=`echo "scale=10; ${trx[$i]} / ${sum_trx}" | bc`
				echo -n "	${site_perc}" >>$data_brkdwn
			done
	
			echo "" >>$data_brkdwn
		done
	
		echo "" >>$data
		echo "" >>$data_aborts
	done
	
	# -- PERF --
	pscript="logs/${DIR}graph_${benchmark}_r${read_all}_w${write_all}_${disjoint}_${comm}.p"
	eps="logs/${DIR}graph_${benchmark}_r${read_all}_w${write_all}_${disjoint}_${comm}.eps"
	
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
	#echo "set yrange [3000:14000]" >>$pscript
	#echo "set xrange [1:7]" >>$pscript
	if [[ $disjoint == "-d" ]]; then
		echo "set title \"${title[1]}, Bank (disjoint)\"" >>$pscript
	else
		echo "set title \"${title[1]}, Bank (contended)\"" >>$pscript
	fi
	echo "set xlabel \"Replicas\"" >>$pscript
	echo "set ylabel \"Transactions/second\"" >>$pscript
	echo "set key bottom right" >>$pscript
	echo "set term postscript eps enhanced color 22" >>$pscript
	echo "set output \"${eps}\"" >>$pscript
	echo "set style line 1 linetype 1 pointtype 1" >>$pscript
	echo "set style line 2 linetype 2 pointtype 2" >>$pscript
	echo "set style line 3 linetype 4 pointtype 4" >>$pscript
	# -- WITH DEV (needs 5 RUNS) --
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
	pscript="logs/${DIR}graph_${benchmark}_r${read_all}_w${write_all}_${disjoint}_${comm}_aborts.p"
	eps="logs/${DIR}graph_${benchmark}_r${read_all}_w${write_all}_${disjoint}_${comm}_aborts.eps"
	
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
	#echo "set yrange [3000:14000]" >>$pscript
	#echo "set xrange [1:7]" >>$pscript
	if [[ $disjoint == "-d" ]]; then
		echo "set title \"${title[1]}, Bank (disjoint)\"" >>$pscript
	else
		echo "set title \"${title[1]}, Bank (contended)\"" >>$pscript
	fi
	echo "set xlabel \"Replicas\"" >>$pscript
	echo "set ylabel \"Aborts (%)\"" >>$pscript
	echo "set key bottom right" >>$pscript
	echo "set term postscript eps enhanced color 22" >>$pscript
	echo "set output \"${eps}\"" >>$pscript
	echo "set style line 1 linetype 1 pointtype 1" >>$pscript
	echo "set style line 2 linetype 2 pointtype 2" >>$pscript
	echo "set style line 3 linetype 4 pointtype 4" >>$pscript
	# -- WITH DEV (needs 5 RUNS) --
		#echo "plot \"${data}\" using 1:2:3:4 with yerrorlines title columnhead(2), \\" >>$pscript
		#echo "'' using 1:5:6:7 with yerrorlines title columnhead(3), \\" >>$pscript
		#echo "'' using 1:8:9:10 with yerrorlines title columnhead(4)" >>$pscript
	# -- WITHOUT DEV --
		echo "plot \"${data_aborts}\" using 1:2 with linespoints linestyle 1 title columnhead(2)" >>$pscript
		#echo "'' using 1:3 with linespoints linestyle 2 title columnhead(3), \\" >>$pscript
		#echo "'' using 1:4 with linespoints linestyle 3 title columnhead(4)" >>$pscript
	
	gnuplot $pscript
	epstopdf $eps

	
	# -- BRKDWN --
	pscript="logs/${DIR}graph_${benchmark}_r${read_all}_w${write_all}_${disjoint}_${comm}_breakdown.p"
	eps="logs/${DIR}graph_${benchmark}_r${read_all}_w${write_all}_${disjoint}_${comm}_breakdown.eps"
	
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
	if [[ $disjoint == "-d" ]]; then
		echo "set title \"${title[1]}, Bank (disjoint)\"" >>$pscript
	else
		echo "set title \"${title[1]}, Bank (contended)\"" >>$pscript
	fi
	echo "set xlabel \"(Replicas, thread/replica)\"" >>$pscript
	echo "set ylabel \"Transacoes (% do total)\"" >>$pscript
	#echo "set key outside right" >>$pscript
	echo "unset key" >>$pscript
	echo "set term postscript eps enhanced color 22" >>$pscript
	echo "set output \"${eps}\"" >>$pscript
	echo "set yrange [0:100]" >>$pscript
	echo "plot for [i=2:9] \"${data_brkdwn}\" using (100.*column(i)):xticlabels(1) title columnhead(i)" >>$pscript
	
	gnuplot $pscript
	epstopdf $eps
	
done
