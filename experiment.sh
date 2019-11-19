#!/bin/bash

cmdpart="/home/azureuser/memtier_benchmark/install/bin/memtier_benchmark --protocol=memcache_text --ratio=0:1 --expiry-range=99999-100000 --hide-histogram"
server=$1
port=$2
time=$3
threads=$4
value_size=$5
clients_number=$6
key_size=$7

cmd="$cmdpart --key-maximum=$key_size --server=$server --port=$port --test-time=$time --data-size=$value_size --clients=$clients_number --threads=$threads"
$cmd
