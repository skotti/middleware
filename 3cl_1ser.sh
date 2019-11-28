#!/bin/bash
source /home/azureuser/asl-project-2019-ruzhanskaia/environment.sh
test_time=$2
repetitions=$3
key_size=$4
memtier_threads=3
value_size=$1
n_clients=(4 8 16 32)

for c in "${n_clients[@]}"; do
    for value in {1..$repetitions}
    do
        # run script on client1
    	ssh azureuser@$client1ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server1ip} 11210 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/client1.log" &
    	pid1=$!
	ssh azureuser@$client2ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server1ip} 11210 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/client2.log" &
	pid2=$!
	ssh azureuser@$client3ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${serveri1p} 11210 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/client3.log" &
	pid3=$!
	wait $pid1
	wait $pid2
	wait $pid3
	sleep 10s
    done
done
echo "DONE 1st part"
sleep 10s
