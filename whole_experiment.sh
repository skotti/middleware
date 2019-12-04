#!/bin/bash
source /home/azureuser/asl-project-2019-ruzhanskaia/environment.sh
populate_port=11210
populate_time=1000
key_size=10000
test_time=70
repetitions=3

value_size=(64 256 512 1024)

# create folders for experiments
ssh azureuser@$client1ip "mkdir part1 part2 part3 part4 part5 part6"
ssh azureuser@$client2ip "mkdir part1 part2 part3 part4 part5 part6"
ssh azureuser@$client3ip "mkdir part1 part2 part3 part4 part5 part6"

for v in "${value_size[@]}"; do
    /home/azureuser/asl-project-2019-ruzhanskaia/populate_3servers.sh $populate_port $v $populate_time $key_size
    /home/azureuser/asl-project-2019-ruzhanskaia/3cl_1ser.sh $v $test_time $repetitions $key_size
    echo "First completed"
    /home/azureuser/asl-project-2019-ruzhanskaia/3cl_3ser.sh $v $test_time $repetitions $key_size
    echo "Second completed"
    /home/azureuser/asl-project-2019-ruzhanskaia/3cl_1mid_1ser.sh $v $test_time $repetitions $key_size
    echo "Third completed"
    /home/azureuser/asl-project-2019-ruzhanskaia/3cl_1mid_3ser.sh $v $test_time $repetitions $key_size
    echo "Fourth completed"
    /home/azureuser/asl-project-2019-ruzhanskaia/3cl_2mid_1ser.sh $v $test_time $repetitions $key_size
    echo "Fifth completed"
    /home/azureuser/asl-project-2019-ruzhanskaia/3cl_2mid_3ser.sh $v $test_time $repetitions $key_size
    echo "Sixth completed"
done
