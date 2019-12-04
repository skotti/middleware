#!/bin/bash
source /home/azureuser/asl-project-2019-ruzhanskaia/environment.sh
test_time=$2
repetitions=$3
key_size=$4
memtier_threads=2
value_size=$1
n_clients=(4 8 16 32)
n_workers=(8 32 64)

for t in "${n_workers[@]}"; do
    ssh azureuser@$middleware1ip "/home/azureuser/asl-project-2019-ruzhanskaia/start.sh ${middleware1ip} 11211 ${t}"
    sleep 10s
    for c in "${n_clients[@]}"; do
        for value in {1..3}
        do
    	# run script on client1
    	ssh azureuser@$client1ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${middleware1ip} 11211 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/part3/client1.log" &
    	pid1=$!
    	ssh azureuser@$client2ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${middleware1ip} 11211 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/part3/client2.log" &
    	pid2=$!
    	ssh azureuser@$client3ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${middleware1ip} 11211 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/part3/client3.log" &
    	pid3=$!
    	# wait script on client1
    	wait $pid1
    	# wait script on client2
    	wait $pid2
    	# wait script on client3
    	wait $pid3
    	sleep 10s
        done
    done
    ssh azureuser@$middleware1ip '/home/azureuser/asl-project-2019-ruzhanskaia/stop.sh'
    sleep 10s
done
