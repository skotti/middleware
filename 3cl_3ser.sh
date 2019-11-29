#!/bin/bash
source /home/azureuser/asl-project-2019-ruzhanskaia/environment.sh
test_time=$2
repetitions=$3
key_size=$4
memtier_threads=1
value_size=$1
n_clients=(4 8 16 32)

for c in "${n_clients[@]}"; do
    for value in {1..$repetitions}
    do
        # run script on client1
        ssh azureuser@$client1ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server1ip} 11210 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/client1_1.log" &
        pid1=$!
        ssh azureuser@$client1ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server2ip} 11210 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/client1_2.log" &
        pid2=$!
        ssh azureuser@$client1ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server3ip} 11210 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/client1_3.log" &
        pid3=$!
        # run script on client2
        ssh azureuser@$client2ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server1ip} 11210 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/client2_1.log" &
        pid4=$!
        ssh azureuser@$client2ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server2ip} 11210 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/client2_2.log" &
        pid5=$!
        ssh azureuser@$client2ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server3ip} 11210 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/client2_3.log" &
        pid6=$!
        # run script on client3
        ssh azureuser@$client3ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server1ip} 11210 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/client3_1.log" &
        pid7=$!
        ssh azureuser@$client3ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server2ip} 11210 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/client3_2.log" &
        pid8=$!
        ssh azureuser@$client3ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server3ip} 11210 ${test_time} ${memtier_threads} ${value_size} ${c} ${key_size} &>> ${HOME}/client3_3.log" &
        pid9=$!
        wait $pid1
        wait $pid2
        wait $pid3
        wait $pid4
        wait $pid5
        wait $pid6
        wait $pid7
        wait $pid8
        wait $pid9
        sleep 10s
    done
done
echo "DONE 2nd part"
sleep 10s
