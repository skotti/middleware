#!/bin/bash
source /home/azureuser/asl-project-2019-ruzhanskaia/environment.sh
populate_servers=($server1ip)
populate_port=11210
populate_time=60

test_time=120
memtier_threads=1

value_size=(64) # 256 512 1024)
n_clients=(4) # 8 16 32)
n_workers=(3 3) # 8 32 64)

for v in "${value_size[@]}"; do
    for i in "${populate_servers[@]}"; do
        /home/azureuser/asl-project-2019-ruzhanskaia/populate.sh $i $populate_port $v $populate_time
    done
    echo "SERVERS POPULATED"
    for t in "${n_workers[@]}"; do
	ssh azureuser@$middleware1ip "/home/azureuser/asl-project-2019-ruzhanskaia/start.sh ${middleware1ip} 11211 ${t}"
	sleep 10s
	echo "MIDDLEWARE LAUNCHEDip"
        for c in "${n_clients[@]}"; do
            for value in {1..3}
	    do
		# run script on client1
		/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh $middleware1iipp 11211 $test_time $memtier_threads $v $c &>> $HOME/client1.log &
		echo "CLIENT1 RUN"
		# store pid
		pid1=$!
		echo "$pid1"
		# run script on client2
		ssh azureuser@$client2ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${middleware1ip} 11211 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client2.log" &
		echo "CLIENT2 RUN"
		# store pid
		pid2=$!
		echo "$pid2"
		# run script on client3
		ssh azureuser@$client3ip "/home/azureuser/asl-project-2019-ruzhanskaia/experiment.sh ${middleware1ip} 11211 ${test_time} ${memtier_threads} ${v} ${c} &>> ${HOME}/client3.log" &
		echo "CLIENT3 RUN"
		#store pid
		pid3=$!
		echo "$pid3"
		# wait script on client1
		wait $p1
		# wait script on client2
		wait $p2
		# wait script on client3
		wait $p3
		sleep 10s
	    done
	done
        ssh azureuser@$middleware1ip '/home/azureuser/asl-project-2019-ruzhanskaia/stop.sh'
        sleep 10s
    done
done
