#!/bin/bash
source /home/azureuser/asl-project-2019-ruzhanskaia/environment.sh
populate_port=11210
populate_time=1000

test_time=180
memtier_threads=1

value_size=(64) # 256 512 1024)
n_clients=(4 8 16 32)

for v in "${value_size[@]}"; do
    /home/azureuser/asl-project-2019-ruzhanskaia/populate_3servers.sh $populate_port $v $populate_time
    for c in "${n_clients[@]}"; do
        for value in {1..3}
	do
	    # run script on client1
	    ssh azureuser@$client1ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server1ip} 11210 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client1_1.log" &
	    pid1=$!
	    ssh azureuser@$client1ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server2ip} 11210 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client1_2.log" &
            pid2=$!
	    ssh azureuser@$client1ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server3ip} 11210 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client1_3.log" &
            pid3=$!
	    # run script on client2
	    ssh azureuser@$client2ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server1ip} 11210 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client2_1.log" &
            pid4=$!
	    ssh azureuser@$client2ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server2ip} 11210 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client2_2.log" &
            pid5=$!
            ssh azureuser@$client2ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server3ip} 11210 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client2_3.log" &
            pid6=$!
	    # run script on client3
	    ssh azureuser@$client3ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${serveri1p} 11210 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client3_1.log" &
            pid7=$!
            ssh azureuser@$client3ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server21p} 11210 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client3_2.log" &
            pid8=$!
            ssh azureuser@$client3ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${server31p} 11210 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client3_3.log" &
	    pid9=$!
	    wait $p1
	    wait $p2
	    wait $p3
	    wait $p4
            wait $p5
            wait $p6
            wait $p7
            wait $p8
            wait $p9
	    sleep 10s
	done
    done
done
