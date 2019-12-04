#!/bin/bash
source /home/azureuser/asl-project-2019-ruzhanskaia/environment.sh
test_time=$2
repetitions=$3
key_size=$4
memtier_threads=1
value_size=$1
n_clients=(4 8 16 32)
n_workers=(8 32 64)

for t in "${n_workers[@]}"; do
    ssh azureuser@$middleware1ip "/home/azureuser/asl-project-2019-ruzhanskaia/start_3servers.sh ${middleware1ip} 11211 ${t}"
    ssh azureuser@$middleware2ip "/home/azureuser/asl-project-2019-ruzhanskaia/start_3servers.sh ${middleware2ip} 11211 ${t}"
    sleep 10s
    for c in "${n_clients[@]}"; do
        for value in {1..3}
        do
    	# run script on client1
    	ssh azureuser@$client1ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${middleware1ip} 11211 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/part6/client1.log" &
    	pid1=$!
        ssh azureuser@$client1ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${middleware2ip} 11211 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/part6/client2.log" &
        pid2=$!
    	ssh azureuser@$client2ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${middleware1ip} 11211 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/part6/client3.log" &
    	pid3=$!
        ssh azureuser@$client2ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${middleware2ip} 11211 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/part6/client4.log" &
        pid4=$!
    	ssh azureuser@$client3ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${middleware1ip} 11211 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/part6/client5.log" &
    	pid5=$!
        ssh azureuser@$client3ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${middleware2ip} 11211 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/part6/client6.log" &
        pid6=$!
    	wait $pid1
    	wait $pid2
    	wait $pid3
        wait $pid4
        wait $pid5
        wait $pid6
    	sleep 10s
        done
    done
    ssh azureuser@$middleware1ip '/home/azureuser/asl-project-2019-ruzhanskaia/stop.sh'
    ssh azureuser@$middleware2ip '/home/azureuser/asl-project-2019-ruzhanskaia/stop.sh'
    sleep 10s
done
