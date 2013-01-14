#!/bin/sh

# java="java -client -Xmx1024m -Xms1024m"
# java="java"
java="java -Xmx4g -Xms4g "

warmup=1000
duration=20000

for r in 1 2 3; do
#for b in LinkedList SkipList RBTree; do
for b in SkipList RBTree; do
#for i in 256 4096 16384; do
for i in 256 4096 16384; do
#for w in 0 20 50; do
for w in 0 20 50; do
for t in 1 2 4 8 12 16; do

# No RO hint
#for c in lsa.Context tl2.Context tl2lm.Context tl2in.Context jvstm.Context mvstm.Context mvstm.global.Context; do
for c in tl2.Context tl2lm.Context; do
log=logs/intset-${b}-${c}-i${i}-w${w}-t${t}.log
CLASSPATH=classes \
  ${java} -javaagent:bin/deuceAgent.jar \
  -Dorg.deuce.transaction.contextClass=org.deuce.transaction.${c} \
  -Dorg.deuce.transaction.mvstm.versions=16 \
  org.deuce.benchmark.Driver -n ${t} -d ${duration} -w ${warmup} \
  org.deuce.benchmark.intset.Benchmark ${b} -r 262144 -i ${i} -w ${w} >> $log 2>&1
done

for c in tl2lm.Context; do
log=logs/intset-${b}-${c}-na-i${i}-w${w}-t${t}.log
CLASSPATH=classes \
  ${java} -javaagent:bin/deuceAgent.jar \
  -Dorg.deuce.transaction.contextClass=org.deuce.transaction.${c} \
  -Dorg.tribustm.arrays=true \
  org.deuce.benchmark.Driver -n ${t} -d ${duration} -w ${warmup} \
  org.deuce.benchmark.intset.Benchmark ${b} -r 262144 -i ${i} -w ${w} >> $log 2>&1
done

# JVSTM with spin lock
#for c in jvstm.Context; do
#log=logs/intset-${b}-${c}.spin-i${i}-w${w}-t${t}.log
#CLASSPATH=classes \
#  ${java} -javaagent:bin/deuceAgent.jar \
#  -Dorg.deuce.transaction.contextClass=org.deuce.transaction.${c} \
#  -Dorg.deuce.transaction.jvstm.spin=true \
#  org.deuce.benchmark.Driver -n ${t} -d ${duration} -w ${warmup} \
#  org.deuce.benchmark.intset.Benchmark ${b} -r 262144 -i ${i} -w ${w} >> $log 2>&1
#done

# Lock
#for c in lock; do
#log=logs/intset-${b}-${c}-i${i}-w${w}-t${t}.log
#CLASSPATH=classes \
#  ${java} -javaagent:bin/deuceAgent.jar \
#  -Dorg.deuce.transaction.global=true \
#  org.deuce.benchmark.Driver -n ${t} -d ${duration} -w ${warmup} \
#  org.deuce.benchmark.intset.Benchmark ${b} -r 262144 -i ${i} -w ${w} >> $log 2>&1
#done

done
done
done
done
done
