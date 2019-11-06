#!/bin/bash

populate_server=$1
populate_port=$2
populate_size=$3
populate_time=$4

echo 'flush_all' | nc $populate_server $populate_port

#/home/azureuser/memtier_benchmark/install/bin/memtier_benchmark --port=$port --protocol=memcache_text --ratio=1:0 --hide-histogram --expiry-range=9999-10000 --key-maximum=10000 --server=$server --test-time=960 --clients=4 --threads=3 --data-size=$size
/home/skotti/asl/memtier_benchmark/install/bin/memtier_benchmark --port=$populate_port --protocol=memcache_text --ratio=1:0 --hide-histogram --expiry-range=9999-10000 --key-maximum=100 --server=$populate_server --test-time=$populate_time --clients=4 --threads=3 --data-size=$populate_size
