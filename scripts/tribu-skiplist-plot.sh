#!/bin/sh

DIR=$1
benchmark=SkipList
size=32768
write=10
for comm in appia.Appia jgroups.JGroups spread.Spread
do

	data="logs/${DIR}${benchmark}_i${size}_w${write}_${comm}.data"
	data_brkdwn="logs/${DIR}${benchmark}_i${size}_w${write}_${comm}_breakdown.data"
	rm -f $data
	rm -f $data_brkdwn

	echo '"Replicas"	"1 thread"	"2 threads"	"4 threads"' >$data
	echo '"(Replicas, threads)"	"R1"	"R2"	"R3"	"R4"	"R5"	"R6"	"R7"	"R8"' >$data_brkdwn
	
	for sites in 2 3 4 5 6 7 8
	do
		echo -n "${sites}" >>$data

		for thread in 1 2 4
		do
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
				run_duration[$run]=0
				ok=0
				for (( site = 1; site <= $sites; site++ ))
				do
					# -- PERF --
					log="logs/${DIR}${benchmark}_i${size}_w${write}_t${thread}_nonvoting.NonVoting_${comm}_id${site}-${sites}_run${run}.res"
					iterations=`grep "Nb iterations" ${log} | awk '{ i = $4 } END { printf "%d", i }'`
			        duration=`grep "Test duration" ${log} | awk '{ d = $5 } END { printf "%d", d/1000 }'`
					run_iterations[$run]=`expr ${run_iterations[$run]} + ${iterations}`

					# -- BRKDWN --
					latency[$site]=`grep -A 1 "TO" ${log} | grep "avg" | awk '{ l = $3 } END { printf "%d", l }'`
					trx[$site]=$iterations
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

			echo "replicas=${sites}, w=${write}%, th=${thread}"
			echo "	max: ${run_iterations[$max]}"
			echo "	min: ${run_iterations[$min]}"

			sum_iterations=0
			brkdwn_run=0
			for (( i = 1; i <= $runs; i++ ))
			do
				# -- 1 RUN --
					#sum_iterations=`expr ${sum_iterations} + ${run_iterations[$i]}`
				# -- 5 RUNS --
					if [[ $i -ne $min && $i -ne $max ]]
					then
						brkdwn_run=$i
						sum_iterations=`expr ${sum_iterations} + ${run_iterations[$i]}`
					fi
			done

			# -- PERF --
			# -- 1 RUN
				#rs=${runs}
			# -- 5 RUNS --
				rs=`expr ${runs} - 2`
			echo "	runs: ${rs}"
			sum_iterations=`expr ${sum_iterations} / ${rs}`
			echo "	iterations: ${sum_iterations}"
			txps=`expr ${sum_iterations} / ${duration}`
			# -- 5 RUNS --
				txps_max=`expr ${run_iterations[$max]} / ${duration}`
				txps_min=`expr ${run_iterations[$min]} / ${duration}`
			echo "	txps: ${txps}"
			# -- WITH DEV (needs 5 RUNS) --
				#echo -n "	${txps}	${txps_min}	${txps_max}" >>${data}
			# -- WITHOUT DEV --
				echo -n "	${txps}" >>$data

			# -- BRKDWN --
			sum_trx=0
			for (( i = 1; i <= $sites; i++ ))
			do
				sum_trx=`expr ${sum_trx} + ${trx[$i]}`
			done
			for (( i = 1; i <= $sites; i++ ))
			do
				site_perc=`echo "scale=10; ${trx[$i]} / ${sum_trx}" | bc`
				echo -n "	${site_perc}" >>$data_brkdwn
			done

			echo "" >>$data_brkdwn
		done

		echo "" >>$data
	done

	# -- PERF --
	pscript="logs/${DIR}graph_${benchmark}_i${size}_w${write}_${comm}.p"
	eps="logs/${DIR}graph_${benchmark}_i${size}_w${write}_${comm}.eps"

	title=(${comm//./ });
	echo "set autoscale" >$pscript
	echo "unset log" >>$pscript
	echo "unset label" >>$pscript
	echo "set boxwidth 0.9 relative" >>$pscript
	#echo "set style data linespoints" >>$pscript
	echo "set style fill solid 1.0 border lt -1" >>$pscript
	echo "set xtics 1" >>$pscript
	echo "set grid ytics" >>$pscript
	#echo "set ytics 2000" >>$pscript
	#echo "set yrange [0:20000]" >>$pscript
	#echo "set xrange [1:7]" >>$pscript
	echo "set title \"${title[1]}, ${benchmark} (size=${size}, update=${write}%)\"" >>$pscript
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

	# -- BRKDWN --
	pscript="logs/${DIR}graph_${benchmark}_i${size}_w${write}_${comm}_breakdown.p"
	eps="logs/${DIR}graph_${benchmark}_i${size}_w${write}_${comm}_breakdown.eps"

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
	echo "set title \"${title[1]}, RBTree (size=${size}, update=${write}%)\"" >>$pscript
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
