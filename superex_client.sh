#!/bin/bash
source /home/azureuser/asl-project-2019-ruzhanskaia/environment.sh
populate_server=$server1ip
populate_port=11210
populate_time=1000

test_time=180
memtier_threads=1

value_size=(64 # 256 512 1024)
n_clients=(4 8 16 32)

for v in "${value_size[@]}"; do
    /home/azureuser/asl-project-2019-ruzhanskaia/populate.sh $populate_server $populate_port $v $populate_time
    for c in "${n_clients[@]}"; do
        for value in {1..3}
	do
	    # run script on client1
	    ssh azureuser@$client1ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server1ip} 11210 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client1.log" &
	    pid1=$!
	    ssh azureuser@$client2ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server1ip} 11210 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client2.log" &
	    pid2=$!
	    ssh azureuser@$client3ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${serveri1p} 11210 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client3.log" &
	    pid3=$!
	    wait $pid1
	    wait $pid2
	    wait $pid3
	    sleep 10s
	done
    done
done
