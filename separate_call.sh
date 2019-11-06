#!/bin/bash
server=$1
port=$2
clients=$3
threads=$4
size=$5
time=$6

/home/skotti/asl/memtier_benchmark/install/bin/memtier_benchmark --port=$port --protocol=memcache_text --ratio=0:1 --hide-histogram --expiry-range=9999-10000 --key-maximum=100 --server=$server --test-time=$time --clients=$clients --threads=3 --data-size=$size
#/home/azureuser/memtier_benchmark/install/bin/memtier_benchmark --port=$port --protocol=memcache_text --ratio=0:1 --hide-histogram --expiry-range=9999-10000 --key-maximum=10000 --server=$server --test-time=$time --clients=$clients --threads=3 --data-size=$size

