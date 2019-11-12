#!/bin/bash

#cmdpart="/home/azureuser/memtier_benchmark/install/bin/memtier_benchmark  --protocol=memcache_text --ratio=0:1 --expiry-range=9999-10000 --key-maximum=10000 --hide-histogram"
cmdpart="/home/skotti/asl/memtier_benchmark/install/bin/memtier_benchmark --protocol=memcache_text --ratio=0:1 --expiry-range=9999-10000 --key-maximum=100 --hide-histogram"

server=$1
populate_server=$2
port=$3
populate_port=$4
time=$5
threads=$6

value_size=(64) # 256 512 1024)
n_clients=(4) # 8 16 32)

for v in "${value_size[@]}"; do
    source populate.sh $populate_server $populate_port $v 60 
    for c in "${n_clients[@]}"; do
        for value in {1..3}
        do
            cmd="$cmdpart --server=$server --port=$port --test-time=$time --data-size=$v --clients=$c --threads=$threads"
	    echo "RUN EXPERIMENT"
            $cmd
	    sleep 10s
        done
    done
done

echo "done"
