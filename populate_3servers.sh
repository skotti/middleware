#!/bin/bash
source /home/azureuser/asl-project-2019-ruzhanskaia/environment.sh
populate_port=$1
populate_size=$2
populate_time=$3
populate_key_size=$4

echo 'flush_all' | nc $server1ip $populate_port
echo 'flush_all' | nc $server2ip $populate_port
echo 'flush_all' | nc $server3ip $populate_port

/home/azureuser/memtier_benchmark/install/bin/memtier_benchmark --port=$populate_port --protocol=memcache_text --ratio=1:0 --hide-histogram --expiry-range=99999-100000 --key-maximum=$populate_key_size --server=$server1ip --test-time=$populate_time --clients=4 --threads=3 --data-size=$populate_size &
pid1=$!
/home/azureuser/memtier_benchmark/install/bin/memtier_benchmark --port=$populate_port --protocol=memcache_text --ratio=1:0 --hide-histogram --expiry-range=99999-100000 --key-maximum=$populate_key_size --server=$server2ip --test-time=$populate_time --clients=4 --threads=3 --data-size=$populate_size &
pid2=$!
/home/azureuser/memtier_benchmark/install/bin/memtier_benchmark --port=$populate_port --protocol=memcache_text --ratio=1:0 --hide-histogram --expiry-range=99999-100000 --key-maximum=$populate_key_size --server=$server3ip --test-time=$populate_time --clients=4 --threads=3 --data-size=$populate_size &
pid3=$!

wait $pid1
wait $pid2
wait $pid3
