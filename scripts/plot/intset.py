#!/usr/bin/python

import sys
import argparse
import math
import matplotlib.pyplot as plt

CTXS = {'tl2.Context': 'TL2', 
	'mvstm.Context': 'MVSTM',
	'score.SCOReContext': 'SCORe'}

PROTOS = {'nonvoting.NonVoting': 'NonVoting',
	'voting.Voting': 'Voting',
	'score.SCOReProtocol': 'SCORe',
	'score.SCOReProtocol_noReadOpt': 'SCORe w/o read opt.',
	'score.SCOReProtocol_cache': 'SCORe w/ cache'}

VERBOSE = False
VISUAL = False
FILE = ''

def full_rep_stats(dir, bench, writes, runs, size, thrs, proto, ctx, comm, 
	replicas, partial_ops):
	run_its = [0] * (runs + 1)
	run_committed = [0] * (runs + 1)
	run_aborted = [0] * (runs + 1)
	run_app = [0] * (runs + 1)
	run_app_max = [0] * (runs + 1)
	run_app_min = [0] * (runs + 1)
	run_val = [0] * (runs + 1)
	run_val_max = [0] * (runs + 1)
	run_val_min = [0] * (runs + 1)
	run_commit = [0] * (runs + 1)
	run_commit_max = [0] * (runs + 1)
	run_commit_min = [0] * (runs + 1)
	run_lreads = [0] * (runs + 1)
	run_lreads_max = [0] * (runs + 1)
	run_lreads_min = [0] * (runs + 1)
	run_ser = [0] * (runs + 1)
	run_ser_max = [0] * (runs + 1)
	run_ser_min = [0] * (runs + 1)
	run_conf = [0] * (runs + 1)
	run_conf_max = [0] * (runs + 1)
	run_conf_min = [0] * (runs + 1)
	run_msgsent = [0] * (runs + 1)
	run_msgsent_size = [0] * (runs + 1)
	run_msgsent_size_max = [0] * (runs + 1)
	run_msgsent_size_min = [0] * (runs + 1)
	run_msgrecv = [0] * (runs + 1)
	run_msgrecv_size = [0] * (runs + 1)
	run_msgrecv_size_max = [0] * (runs + 1)
	run_msgrecv_size_min = [0] * (runs + 1)
	max = 1
	min = 1
	
	for run in xrange(1, runs + 1):
		for site in xrange(1, replicas + 1):
			log = '%s/%s_i%d_w%d_t%d_%s_%s_%s_id%d-%d_run%d_%d.res' % (dir, 
				bench, size, writes, thrs, proto, ctx, comm, site, replicas, 
				run, partial_ops)
			file = open(log, 'r').readlines()
			
			its = [x for x in file if 'Nb iterations' in x]
			run_its[run] += int(its[0].split('=')[1])
			
			dur = [x for x in file if 'Test duration' in x]
			dur = int(dur[0].split('=')[1]) / 1000
			
			committed = [x for x in file if 'Committed' in x]
			run_committed[run] += int(committed[0].split('=')[1])
			
			aborted = [x for x in file if 'Aborted' in x]
			aborted = int(aborted[0].split('=')[1])
			run_aborted[run] += aborted
			
			# in microseconds
			app_dur = file.index('  Tx App Duration\n')
			run_app[run] += float(file[app_dur + 1].split()[2])
			run_app_max[run] += float(file[app_dur + 2].split()[2])
			run_app_min[run] += float(file[app_dur + 3].split()[2])
			
			# in microseconds
			val_op = file.index('  Validation Operation\n')
			run_val[run] += float(file[val_op + 1].split()[2])
			run_val_max[run] += float(file[val_op + 2].split()[2])
			run_val_min[run] += float(file[val_op + 3].split()[2])
			
			# in microseconds
			commit_op = file.index('  Commit Operation\n')
			run_commit[run] += float(file[commit_op + 1].split()[2])
			run_commit_max[run] += float(file[commit_op + 2].split()[2])
			run_commit_min[run] += float(file[commit_op + 3].split()[2])
			
			# in microseconds
			lreads = file.index('    --Local Reads\n')
			run_lreads[run] += float(file[lreads + 1].split()[2])
			run_lreads_max[run] += float(file[lreads + 2].split()[2])
			run_lreads_min[run] += float(file[lreads + 3].split()[2])
			
			# in miliseconds
			ser = file.index('  Serialization\n')
			run_ser[run] += float(file[ser + 1].split()[2])
			run_ser_max[run] += float(file[ser + 2].split()[2])
			run_ser_min[run] += float(file[ser + 3].split()[2])
			
			# in miliseconds
			conf = file.index('  Confirmation\n')
			run_conf[run] += float(file[conf + 1].split()[2])
			run_conf_max[run] += float(file[conf + 2].split()[2])
			run_conf_min[run] += float(file[conf + 3].split()[2])
			
			# in bytes
			msg_sent = [i for i, s in enumerate(file) if 'Msgs Sent' in s][0]
			run_msgsent[run] += int(file[msg_sent].split()[3])
			run_msgsent_size[run] += int(file[msg_sent + 1].split()[2])
			run_msgsent_size_max[run] += int(file[msg_sent + 2].split()[2])
			run_msgsent_size_min[run] += int(file[msg_sent + 3].split()[2])
			
			# in bytes
			msg_recv = [i for i, s in enumerate(file) if 'Msgs Recv' in s][0]
			run_msgrecv[run] += int(file[msg_recv].split()[3])
			run_msgrecv_size[run] += int(file[msg_recv + 1].split()[2])
			run_msgrecv_size_max[run] += int(file[msg_recv + 2].split()[2])
			run_msgrecv_size_min[run] += int(file[msg_recv + 3].split()[2])
		#end for
		
		if run_its[run] > run_its[max]:
			max = run
		if run_its[run] < run_its[min]:
			min = run
	#end for
	
	print 'replicas=%d, w=%d%%, th=%d, pops=%d%%' % (replicas, writes, 
		thrs, partial_ops)
	print '  max: %d' % (run_its[max])
	print '  min: %d' % (run_its[min])
	
	# sum stuff
	sum_its = 0
	sum_commits = 0
	sum_aborts = 0
	sum_app = 0
	sum_app_max = 0
	sum_app_min = 0
	sum_val = 0
	sum_val_max = 0
	sum_val_min = 0
	sum_commit = 0
	sum_commit_max = 0
	sum_commit_min = 0
	sum_lreads = 0
	sum_lreads_max = 0
	sum_lreads_min = 0
	sum_ser = 0
	sum_ser_max = 0
	sum_ser_min = 0
	sum_conf = 0
	sum_conf_max = 0
	sum_conf_min = 0
	sum_msgsent = 0
	sum_msgsent_size = 0
	sum_msgsent_size_max = 0
	sum_msgsent_size_min = 0
	sum_msgrecv = 0
	sum_msgrecv_size = 0
	sum_msgrecv_size_max = 0
	sum_msgrecv_size_min = 0
	for i in xrange(1, runs + 1):
		if i != min and i != max:
			sum_its += run_its[i]
			sum_commits += run_committed[i]
			sum_aborts += run_aborted[i]
			sum_app += run_app[i]
			sum_app_max += run_app_max[i]
			sum_app_min += run_app_min[i]
			sum_val += run_val[i]
			sum_val_max += run_val_max[i]
			sum_val_min += run_val_min[i]
			sum_commit += run_commit[i]
			sum_commit_max += run_commit_max[i]
			sum_commit_min += run_commit_min[i]
			sum_lreads += run_lreads[i]
			sum_lreads_max += run_lreads_max[i]
			sum_lreads_min += run_lreads_min[i]
			sum_ser += run_ser[i]
			sum_ser_max += run_ser_max[i]
			sum_ser_min += run_ser_min[i]
			sum_conf += run_conf[i]
			sum_conf_max += run_conf_max[i]
			sum_conf_min += run_conf_min[i]
			sum_msgsent += run_msgsent[i]
			sum_msgsent_size += run_msgsent_size[i]
			sum_msgsent_size_max += run_msgsent_size_max[i]
			sum_msgsent_size_min += run_msgsent_size_min[i]
			sum_msgrecv += run_msgrecv[i]
			sum_msgrecv_size += run_msgrecv_size[i]
			sum_msgrecv_size_max += run_msgrecv_size_max[i]
			sum_msgrecv_size_min += run_msgrecv_size_min[i]
	#end for
	
	rs = runs
	if max == min:
		rs -= 1
	else:
		rs -= 2
	
	print '  runs: %d (%d s)' % (rs, dur)
	sum_its /= (rs * 1.0)
	print '  its avg: %.3f' % (sum_its)
	txps = int(sum_its / dur)
	
	var = 0
	for i in xrange(1, runs + 1):
		if i != min and i != max:
			var += (run_its[i] - sum_its) ** 2
	#end for
	
	std = math.sqrt(var / (rs * 1.0))
	std /= dur
	print '  txps: %d (std: %.3f)' % (txps, std)
	
	sum_commits /= (rs * 1.0)
	sum_aborts /= (rs * 1.0)
	abort_rate = (sum_aborts * 100) / sum_commits
	print '  abort rate: %.3f%%' % (abort_rate)
	
	sum_app /= (rs * 1.0)
	sum_app_max /= (rs * 1.0)
	sum_app_min /= (rs * 1.0)
	if VERBOSE:
		print '  tx app duration: %.3f micro' % (sum_app)
		print '    max: %.3f micro' % (sum_app_max)
		print '    min: %.3f micro' % (sum_app_min)
	
	sum_val /= (rs * 1.0)
	sum_val_max /= (rs * 1.0)
	sum_val_min /= (rs * 1.0)
	if VERBOSE:
		print '  validation: %.3f micro' % (sum_val)
		print '    max: %.3f micro' % (sum_val_max)
		print '    min: %.3f micro' % (sum_val_min)
	
	sum_commit /= (rs * 1.0)
	sum_commit_max /= (rs * 1.0)
	sum_commit_min /= (rs * 1.0)
	if VERBOSE:
		print '  commit: %.3f micro' % (sum_commit)
		print '    max: %.3f micro' % (sum_commit_max)
		print '    min: %.3f micro' % (sum_commit_min)
	
	sum_lreads /= (rs * 1.0)
	sum_lreads_max /= (rs * 1.0)
	sum_lreads_min /= (rs * 1.0)
	if VERBOSE:
		print '  local reads: %.3f micro' % (sum_lreads)
		print '    max: %.3f micro' % (sum_lreads_max)
		print '    min: %.3f micro' % (sum_lreads_min)
	
	sum_ser /= (rs * 1.0)
	sum_ser_max /= (rs * 1.0)
	sum_ser_min /= (rs * 1.0)
	if VERBOSE:
		print '  serialization: %.3f ms' % (sum_ser)
		print '    max: %.3f ms' % (sum_ser_max)
		print '    min: %.3f ms' % (sum_ser_min)
	
	sum_conf /= (rs * 1.0)
	sum_conf_max /= (rs * 1.0)
	sum_conf_min /= (rs * 1.0)
	if VERBOSE:
		print '  confirmation: %.3f ms' % (sum_conf)
		print '    max: %.3f ms' % (sum_conf_max)
		print '    min: %.3f ms' % (sum_conf_min)
	
	sum_msgsent /= rs
	sum_msgsent_size /= rs
	sum_msgsent_size_max /= rs
	sum_msgsent_size_min /= rs
	if VERBOSE:
		print '  msgs sent: %d (size avg: %d bytes)' % (sum_msgsent, 
			sum_msgsent_size)
		print '    max: %d bytes' % (sum_msgsent_size_max)
		print '    min: %d bytes' % (sum_msgsent_size_min)
	
	sum_msgrecv /= rs
	sum_msgrecv_size /= rs
	sum_msgrecv_size_max /= rs
	sum_msgrecv_size_min /= rs
	if VERBOSE:
		print '  msgs recv: %d (size avg: %d bytes)' % (sum_msgrecv, 
			sum_msgrecv_size)
		print '    max: %d bytes' % (sum_msgrecv_size_max)
		print '    min: %d bytes' % (sum_msgrecv_size_min)
	
	return (txps, std, abort_rate, 
		[sum_app, sum_app_max, sum_app_min], 
		[sum_val, sum_val_max, sum_val_min], 
		[sum_commit, sum_commit_max, sum_commit_min], 
		[sum_lreads, sum_lreads_max, sum_lreads_min], 
		[sum_ser, sum_ser_max, sum_ser_min], 
		[sum_conf, sum_conf_max, sum_conf_min], 
		[sum_msgsent, sum_msgsent_size, sum_msgsent_size_max, sum_msgsent_size_min], 
		[sum_msgrecv, sum_msgrecv_size_max, sum_msgrecv_size_min])
#end function

def par_rep_stats(dir, bench, writes, runs, size, thrs, proto, ctx, comm, 
	replicas, partial_ops):
	run_its = [0] * (runs + 1)
	run_committed = [0] * (runs + 1)
	run_aborted = [0] * (runs + 1)
	run_app = [0] * (runs + 1)
	run_app_max = [0] * (runs + 1)
	run_app_min = [0] * (runs + 1)
	run_val = [0] * (runs + 1)
	run_val_max = [0] * (runs + 1)
	run_val_min = [0] * (runs + 1)
	run_commit = [0] * (runs + 1)
	run_commit_max = [0] * (runs + 1)
	run_commit_min = [0] * (runs + 1)
	run_lreads = [0] * (runs + 1)
	run_lreads_max = [0] * (runs + 1)
	run_lreads_min = [0] * (runs + 1)
	run_ser = [0] * (runs + 1)
	run_ser_max = [0] * (runs + 1)
	run_ser_min = [0] * (runs + 1)
	run_conf = [0] * (runs + 1)
	run_conf_max = [0] * (runs + 1)
	run_conf_min = [0] * (runs + 1)
	run_msgsent = [0] * (runs + 1)
	run_msgsent_size = [0] * (runs + 1)
	run_msgsent_size_max = [0] * (runs + 1)
	run_msgsent_size_min = [0] * (runs + 1)
	run_msgrecv = [0] * (runs + 1)
	run_msgrecv_size = [0] * (runs + 1)
	run_msgrecv_size_max = [0] * (runs + 1)
	run_msgrecv_size_min = [0] * (runs + 1)
	max = 1
	min = 1
	
	groups = 4
	dpart= 'RoundRobin'
	
	for run in xrange(1, runs + 1):
		for site in xrange(1, replicas + 1):
			log = '%s/%s_i%d_w%d_t%d_%s_%s_%s_id%d-%d_run%d_g%d_%s_%d.res' % (dir, 
				bench, size, writes, thrs, proto, ctx, comm, site, replicas, 
				run, groups, dpart, partial_ops)
			file = open(log, 'r').readlines()
			
			its = [x for x in file if 'Nb iterations' in x]
			run_its[run] += int(its[0].split('=')[1])
			
			dur = [x for x in file if 'Test duration' in x]
			dur = int(dur[0].split('=')[1]) / 1000
			
			committed = [x for x in file if 'Committed' in x]
			run_committed[run] += int(committed[0].split('=')[1])
			
			aborted = [x for x in file if 'Aborted' in x]
			aborted = int(aborted[0].split('=')[1])
			run_aborted[run] += aborted
			
			# in microseconds
			app_dur = file.index('  Tx App Duration\n')
			run_app[run] += float(file[app_dur + 1].split()[2])
			run_app_max[run] += float(file[app_dur + 2].split()[2])
			run_app_min[run] += float(file[app_dur + 3].split()[2])
			
			# in microseconds
			val_op = file.index('  Validation Operation\n')
			run_val[run] += float(file[val_op + 1].split()[2])
			run_val_max[run] += float(file[val_op + 2].split()[2])
			run_val_min[run] += float(file[val_op + 3].split()[2])
			
			# in microseconds
			commit_op = file.index('  Commit Operation\n')
			run_commit[run] += float(file[commit_op + 1].split()[2])
			run_commit_max[run] += float(file[commit_op + 2].split()[2])
			run_commit_min[run] += float(file[commit_op + 3].split()[2])
			
			# in microseconds
			lreads = file.index('    --Local Reads\n')
			run_lreads[run] += float(file[lreads + 1].split()[2])
			run_lreads_max[run] += float(file[lreads + 2].split()[2])
			run_lreads_min[run] += float(file[lreads + 3].split()[2])
			
			# in miliseconds
			ser = file.index('  Serialization\n')
			run_ser[run] += float(file[ser + 1].split()[2])
			run_ser_max[run] += float(file[ser + 2].split()[2])
			run_ser_min[run] += float(file[ser + 3].split()[2])
			
			# in miliseconds
			conf = file.index('  Confirmation\n')
			run_conf[run] += float(file[conf + 1].split()[2])
			run_conf_max[run] += float(file[conf + 2].split()[2])
			run_conf_min[run] += float(file[conf + 3].split()[2])
			
			# in bytes
			msg_sent = [i for i, s in enumerate(file) if 'Msgs Sent' in s][0]
			run_msgsent[run] += int(file[msg_sent].split()[3])
			run_msgsent_size[run] += int(file[msg_sent + 1].split()[2])
			run_msgsent_size_max[run] += int(file[msg_sent + 2].split()[2])
			run_msgsent_size_min[run] += int(file[msg_sent + 3].split()[2])
			
			# in bytes
			msg_recv = [i for i, s in enumerate(file) if 'Msgs Recv' in s][0]
			run_msgrecv[run] += int(file[msg_recv].split()[3])
			run_msgrecv_size[run] += int(file[msg_recv + 1].split()[2])
			run_msgrecv_size_max[run] += int(file[msg_recv + 2].split()[2])
			run_msgrecv_size_min[run] += int(file[msg_recv + 3].split()[2])
		#end for
		
		if run_its[run] > run_its[max]:
			max = run
		if run_its[run] < run_its[min]:
			min = run
	#end for
	
	print 'replicas=%d, w=%d%%, th=%d, pops=%d%%' % (replicas, writes, 
		thrs, partial_ops)
	print '  max: %d' % (run_its[max])
	print '  min: %d' % (run_its[min])
	
	# sum stuff
	sum_its = 0
	sum_commits = 0
	sum_aborts = 0
	sum_app = 0
	sum_app_max = 0
	sum_app_min = 0
	sum_val = 0
	sum_val_max = 0
	sum_val_min = 0
	sum_commit = 0
	sum_commit_max = 0
	sum_commit_min = 0
	sum_lreads = 0
	sum_lreads_max = 0
	sum_lreads_min = 0
	sum_ser = 0
	sum_ser_max = 0
	sum_ser_min = 0
	sum_conf = 0
	sum_conf_max = 0
	sum_conf_min = 0
	sum_msgsent = 0
	sum_msgsent_size = 0
	sum_msgsent_size_max = 0
	sum_msgsent_size_min = 0
	sum_msgrecv = 0
	sum_msgrecv_size = 0
	sum_msgrecv_size_max = 0
	sum_msgrecv_size_min = 0
	for i in xrange(1, runs + 1):
		if i != min and i != max:
			sum_its += run_its[i]
			sum_commits += run_committed[i]
			sum_aborts += run_aborted[i]
			sum_app += run_app[i]
			sum_app_max += run_app_max[i]
			sum_app_min += run_app_min[i]
			sum_val += run_val[i]
			sum_val_max += run_val_max[i]
			sum_val_min += run_val_min[i]
			sum_commit += run_commit[i]
			sum_commit_max += run_commit_max[i]
			sum_commit_min += run_commit_min[i]
			sum_lreads += run_lreads[i]
			sum_lreads_max += run_lreads_max[i]
			sum_lreads_min += run_lreads_min[i]
			sum_ser += run_ser[i]
			sum_ser_max += run_ser_max[i]
			sum_ser_min += run_ser_min[i]
			sum_conf += run_conf[i]
			sum_conf_max += run_conf_max[i]
			sum_conf_min += run_conf_min[i]
			sum_msgsent += run_msgsent[i]
			sum_msgsent_size += run_msgsent_size[i]
			sum_msgsent_size_max += run_msgsent_size_max[i]
			sum_msgsent_size_min += run_msgsent_size_min[i]
			sum_msgrecv += run_msgrecv[i]
			sum_msgrecv_size += run_msgrecv_size[i]
			sum_msgrecv_size_max += run_msgrecv_size_max[i]
			sum_msgrecv_size_min += run_msgrecv_size_min[i]
	#end for
	
	rs = runs
	if max == min:
		rs -= 1
	else:
		rs -= 2
	
	print '  runs: %d (%d s)' % (rs, dur)
	sum_its /= (rs * 1.0)
	print '  its avg: %.3f' % (sum_its)
	txps = int(sum_its / dur)
	
	var = 0
	for i in xrange(1, runs + 1):
		if i != min and i != max:
			var += (run_its[i] - sum_its) ** 2
	#end for
	
	std = math.sqrt(var / (rs * 1.0))
	std /= dur
	print '  txps: %d (std: %.3f)' % (txps, std)
	
	sum_commits /= (rs * 1.0)
	sum_aborts /= (rs * 1.0)
	abort_rate = (sum_aborts * 100) / sum_commits
	print '  abort rate: %.3f%%' % (abort_rate)
	
	sum_app /= (rs * 1.0)
	sum_app_max /= (rs * 1.0)
	sum_app_min /= (rs * 1.0)
	if VERBOSE:
		print '  tx app duration: %.3f micro' % (sum_app)
		print '    max: %.3f micro' % (sum_app_max)
		print '    min: %.3f micro' % (sum_app_min)
	
	sum_val /= (rs * 1.0)
	sum_val_max /= (rs * 1.0)
	sum_val_min /= (rs * 1.0)
	if VERBOSE:
		print '  validation: %.3f micro' % (sum_val)
		print '    max: %.3f micro' % (sum_val_max)
		print '    min: %.3f micro' % (sum_val_min)
	
	sum_commit /= (rs * 1.0)
	sum_commit_max /= (rs * 1.0)
	sum_commit_min /= (rs * 1.0)
	if VERBOSE:
		print '  commit: %.3f micro' % (sum_commit)
		print '    max: %.3f micro' % (sum_commit_max)
		print '    min: %.3f micro' % (sum_commit_min)
	
	sum_lreads /= (rs * 1.0)
	sum_lreads_max /= (rs * 1.0)
	sum_lreads_min /= (rs * 1.0)
	if VERBOSE:
		print '  local reads: %.3f micro' % (sum_lreads)
		print '    max: %.3f micro' % (sum_lreads_max)
		print '    min: %.3f micro' % (sum_lreads_min)
	
	sum_ser /= (rs * 1.0)
	sum_ser_max /= (rs * 1.0)
	sum_ser_min /= (rs * 1.0)
	if VERBOSE:
		print '  serialization: %.3f ms' % (sum_ser)
		print '    max: %.3f ms' % (sum_ser_max)
		print '    min: %.3f ms' % (sum_ser_min)
	
	sum_conf /= (rs * 1.0)
	sum_conf_max /= (rs * 1.0)
	sum_conf_min /= (rs * 1.0)
	if VERBOSE:
		print '  confirmation: %.3f ms' % (sum_conf)
		print '    max: %.3f ms' % (sum_conf_max)
		print '    min: %.3f ms' % (sum_conf_min)
	
	sum_msgsent /= rs
	sum_msgsent_size /= rs
	sum_msgsent_size_max /= rs
	sum_msgsent_size_min /= rs
	if VERBOSE:
		print '  msgs sent: %d (size avg: %d bytes)' % (sum_msgsent, 
			sum_msgsent_size)
		print '    max: %d bytes' % (sum_msgsent_size_max)
		print '    min: %d bytes' % (sum_msgsent_size_min)
	
	sum_msgrecv /= rs
	sum_msgrecv_size /= rs
	sum_msgrecv_size_max /= rs
	sum_msgrecv_size_min /= rs
	if VERBOSE:
		print '  msgs recv: %d (size avg: %d bytes)' % (sum_msgrecv, 
			sum_msgrecv_size)
		print '    max: %d bytes' % (sum_msgrecv_size_max)
		print '    min: %d bytes' % (sum_msgrecv_size_min)
	
	return (txps, std, abort_rate, 
		[sum_app, sum_app_max, sum_app_min], 
		[sum_val, sum_val_max, sum_val_min], 
		[sum_commit, sum_commit_max, sum_commit_min], 
		[sum_lreads, sum_lreads_max, sum_lreads_min], 
		[sum_ser, sum_ser_max, sum_ser_min], 
		[sum_conf, sum_conf_max, sum_conf_min], 
		[sum_msgsent, sum_msgsent_size, sum_msgsent_size_max, sum_msgsent_size_min], 
		[sum_msgrecv, sum_msgrecv_size_max, sum_msgrecv_size_min])
#end function

def plot_Y():
	pass
#end function

def plot_Z():
	pass
#end function

def create_parser():
	parser = argparse.ArgumentParser()
	parser.add_argument('-p1', '--performance1', help='plot throughput chart comparing writes and partial_ops in full replication', 
		action='store_true')
	parser.add_argument('-p2', '--performance2', help='plot throughput chart comparing groups and partial_ops in partial replication (for a specified write percentage)', 
		action='store_true')
	
	parser.add_argument('-vv', '--verbose', 
		help='output is more verbose', action='store_true')
	parser.add_argument('-v', '--visual', 
		help='outputs the chart to the screen', action='store_true')
	parser.add_argument('-f', '--file', help='outputs the chart to a pdf file', 
		nargs='?', const='', default='')
	parser.add_argument('-c', '--context', 
		help='what context should I use for full replication?', nargs='?', 
		const='mvstm.Context', default='mvstm.Context')
	parser.add_argument('-pf', '--protofull', 
		help='what protocol should I use for full replication?', nargs='?', 
		const='nonvoting.NonVoting', default='nonvoting.NonVoting')
	parser.add_argument('-pp', '--protopartial', 
		help='what protocol should I use for partial replication?', nargs='?', 
		const='score.SCOReProtocol', default='score.SCOReProtocol')
	parser.add_argument('-r', '--replicas', 
		help='how many replicas where used?', nargs='?', const=8, default=8, 
		type=int)
	parser.add_argument('-rr', '--runs', help='how many runs where used?', 
		nargs='?', const=10, default=10, type=int)
	parser.add_argument('-t', '--threads', help='how many threads where used?', 
		nargs='?', const=4, default=4, type=int)
	parser.add_argument('-b', '--benchmark', help='which benchmark was used?', 
		nargs='?', const='RedBTreeZ', default='RedBTreeZ')
	parser.add_argument('-comm', '--communication', help='which gcs was used?', 
		nargs='?', const='jgroups.JGroups', default='jgroups.JGroups')
	parser.add_argument('-pops', '--partial_ops', help='how many partial ops?', 
		nargs='?', const=50, default=50, type=int)
	parser.add_argument('-w', '--writes', help='how many writes?', nargs='?', 
		const=10, default=10, type=int)
	parser.add_argument('-s', '--size', help='what was the size?', nargs='?', 
		const=32768, default=32768, type=int)
	parser.add_argument('-d', '--directory', help='where are the logs?', nargs='?', 
		const='logs', default='logs')
	
	return parser
#end function

def main(argv):
	parser = create_parser()
	args = parser.parse_args()
	
	global VERBOSE
	VERBOSE = args.verbose # False
	global VISUAL
	VISUAL = args.visual # False
	global FILE
	FILE = args.file # ''
	
	_bench = args.benchmark # RedBTreeZ
	_comm = args.communication # jgroups.JGroups
	_ctx_full = args.context # mvstm.Context
	_ctx_par = 'score.SCOReContext'
	_proto_full = args.protofull # nonvoting.NonVoting
	_proto_par = args.protopartial # score.SCOReProtocol
	_dir = args.directory # logs
	
	_reps = args.replicas # 8
	_runs = args.runs # 10
	_size = args.size # 32768
	_thrs = args.threads # 4
	_writes = args.writes # 10
	_partial_ops = args.partial_ops # 50
	
	if args.performance1:
		#plot_performance1(_dir, _bench, _runs, _size, _thrs, 
		#	_proto_full, _ctx_full, _comm, _reps)
		plot_performance2(_dir, _bench, _runs, _size, _thrs, 
			_proto_par, _ctx_par, _comm, _reps)
	
	plot_performance3(_dir, _bench, _runs, _size, _thrs, _proto_full,
			_proto_par, _ctx_full, _ctx_par, _comm, _reps)
#end function

def plot_performance1(dir, bench, runs, size, thrs, proto, ctx, comm, 
	reps):
	pops = [0, 10, 50, 80, 100]
	for writes in [10, 50]:
		i = 0;
		x = [0] * 5
		for partial in pops:
			stats = full_rep_stats(dir, bench, writes, runs, size, thrs, 
				proto, ctx, comm, reps, partial)
			x[i] = stats[0]
			i += 1
		#end for
		
		str = '%d%% writes' % (writes)
		plt.plot(pops, x, label=str, marker='o')
	#end for
	
	plt.ylabel('Tx/s')
	plt.xlabel('Partial ops (%)')
	title = '%s (%d threads)' % (bench, thrs)
	plt.title(title)
	plt.legend(loc=1)
	plt.grid(True)
	
	if len(FILE) > 0:
		filename = '%s.pdf' % (FILE)
		plt.savefig(filename, bbox_inches='tight')
	
	if VISUAL:
		plt.show()
#end function

def plot_performance2(dir, bench, runs, size, thrs, proto, ctx, comm, 
	reps):
	pops = [0, 10, 50, 80, 100]
	for writes in [10, 50]:
		i = 0;
		x = [0] * 5
		for partial in pops:
			stats = par_rep_stats(dir, bench, writes, runs, size, thrs, 
				proto, ctx, comm, reps, partial)
			x[i] = stats[0]
			i += 1
		#end for
		
		str = '%d%% writes' % (writes)
		plt.plot(pops, x, label=str, marker='o')
	#end for
	
	plt.ylabel('Tx/s')
	plt.xlabel('Partial ops (%)')
	title = '%s (%d threads)' % (bench, thrs)
	plt.title(title)
	plt.legend(loc=1)
	plt.grid(True)
	
	if len(FILE) > 0:
		filename = '%s.pdf' % (FILE)
		plt.savefig(filename, bbox_inches='tight')
	
	if VISUAL:
		plt.show()
#end function

def plot_performance3(dir, bench, runs, size, thrs, proto_full,
	proto_par, ctx_full, ctx_par, comm, reps):
	pops = [0, 10, 50, 80, 100]
	for writes in [10, 50]:
		i = 0;
		x = [0] * 5
		y = [0] * 5
		for partial in pops:
			stats1 = full_rep_stats(dir, bench, writes, runs, size, thrs, 
				proto_full, ctx_full, comm, reps, partial)
			stats2 = par_rep_stats(dir, bench, writes, runs, size, thrs, 
				proto_par, ctx_par, comm, reps, partial)
			x[i] = stats1[0]
			y[i] = stats2[0]
			i += 1
		#end for
		
		str = 'MVSTM %d%% writes' % (writes)
		plt.plot(pops, x, label=str, marker='o')
		str = 'SCORe %d%% writes' % (writes)
		plt.plot(pops, y, label=str, marker='o')
	#end for
	
	plt.ylabel('Tx/s')
	plt.xlabel('Partial ops (%)')
	title = '%s (%d threads)' % (bench, thrs)
	plt.title(title)
	plt.legend(loc=1)
	plt.grid(True)
	
	if len(FILE) > 0:
		filename = '%s.pdf' % (FILE)
		plt.savefig(filename, bbox_inches='tight')
	
	if VISUAL:
		plt.show()
#end function

if __name__ == "__main__":
	main(sys.argv)

