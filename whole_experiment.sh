#!/bin/bash
source /home/azureuser/asl-project-2019-ruzhanskaia/environment.sh
populate_port=11210
populate_time=100
key_size=100
test_time=60
repetitions=3

value_size=(64 256 512 1024)

for v in "${value_size[@]}"; do
    /home/azureuser/asl-project-2019-ruzhanskaia/populate_3servers.sh $populate_port $v $populate_time $key_size
    /home/azureuser/asl-project-2019-ruzhanskaia/3cl_1ser.sh $v $test_time $repetitions $key_size
    /home/azureuser/asl-project-2019-ruzhanskaia/3cl_3ser.sh $v $test_time $repetitions $key_size
    /home/azureuser/asl-project-2019-ruzhanskaia/3cl_1mid_1ser.sh $v $test_time $repetitions $key_size
    /home/azureuser/asl-project-2019-ruzhanskaia/3cl_1mid_3ser.sh $v $test_time $repetitions $key_size
    /home/azureuser/asl-project-2019-ruzhanskaia/3cl_2mid_1ser.sh $v $test_time $repetitions $key_size
    /home/azureuser/asl-project-2019-ruzhanskaia/3cl_2mid_3ser.sh $v $test_time $repetitions $key_size
done
