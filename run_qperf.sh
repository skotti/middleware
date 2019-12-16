#!/bin/bash
source /home/skotti/asl-project-2019-ruzhanskaia/environment.sh

ssh azureuser@$server1 "./qperf-0.4.9/src/qperf" &
ssh azureuser@$client1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.7 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client1:/home/azureuser/result.txt qperf_results/client1_server1.txt
ssh azureuser@$client2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.7 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client1:/home/azureuser/result.txt qperf_results/client2_server1.txt
ssh azureuser@$client3 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.7 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client1:/home/azureuser/result.txt qperf_results/client3_server1.txt
ssh azureuser@$middleware1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.7 tcp_bw tcp_lat &> result.txt"
scp azureuser@$middleware1:/home/azureuser/result.txt qperf_results/middleware1_server1.txt
ssh azureuser@$middleware2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.7 tcp_bw tcp_lat &> result.txt"
scp azureuser@$middleware2:/home/azureuser/result.txt qperf_results/middleware2_server1.txt
ssh azureuser@$server1 "kill -9 \$(ps -aux | grep qperf | awk '{print \$2}')"

ssh azureuser@$server2 "./qperf-0.4.9/src/qperf" &
ssh azureuser@$client1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.9 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client1:/home/azureuser/result.txt qperf_results/client1_server2.txt
ssh azureuser@$client2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.9 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client1:/home/azureuser/result.txt qperf_results/client2_server2.txt
ssh azureuser@$client3 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.9 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client1:/home/azureuser/result.txt qperf_results/client3_server2.txt
ssh azureuser@$middleware1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.9 tcp_bw tcp_lat &> result.txt"
scp azureuser@$middleware1:/home/azureuser/result.txt qperf_results/middleware1_server2.txt
ssh azureuser@$middleware2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.9 tcp_bw tcp_lat &> result.txt"
scp azureuser@$middleware2:/home/azureuser/result.txt qperf_results/middleware2_server2.txt
ssh azureuser@$server2 "kill -9 \$(ps -aux | grep qperf | awk '{print \$2}')"


ssh azureuser@$server3 "./qperf-0.4.9/src/qperf" &
ssh azureuser@$client1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.4 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client1:/home/azureuser/result.txt qperf_results/client1_server3.txt
ssh azureuser@$client2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.4 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client1:/home/azureuser/result.txt qperf_results/client2_server3.txt
ssh azureuser@$client3 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.4 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client1:/home/azureuser/result.txt qperf_results/client3_server3.txt
ssh azureuser@$middleware1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.4 tcp_bw tcp_lat &> result.txt"
scp azureuser@$middleware1:/home/azureuser/result.txt qperf_results/middleware1_server3.txt
ssh azureuser@$middleware2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.4 tcp_bw tcp_lat &> result.txt"
scp azureuser@$middleware2:/home/azureuser/result.txt qperf_results/middleware2_server3.txt
ssh azureuser@$server3 "kill -9 \$(ps -aux | grep qperf | awk '{print \$2}')"


ssh azureuser@$middleware1 "./qperf-0.4.9/src/qperf" &
ssh azureuser@$server1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.5 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server1:/home/azureuser/result.txt qperf_results/server1_middleware1.txt
ssh azureuser@$server2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.5 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server2:/home/azureuser/result.txt qperf_results/server2_middleware1.txt
ssh azureuser@$server3 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.5 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server3:/home/azureuser/result.txt qperf_results/server3_middleware1.txt
ssh azureuser@$client1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.5 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client1:/home/azureuser/result.txt qperf_results/client1_middleware1.txt
ssh azureuser@$client2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.5 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client2:/home/azureuser/result.txt qperf_results/client2_middleware1.txt
ssh azureuser@$client3 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.5 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client3:/home/azureuser/result.txt qperf_results/client3_middleware1.txt
ssh azureuser@$middleware1 "kill -9 \$(ps -aux | grep qperf | awk '{print \$2}')"

ssh azureuser@$middleware2 "./qperf-0.4.9/src/qperf" &
ssh azureuser@$server1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.8 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server1:/home/azureuser/result.txt qperf_results/server1_middleware2.txt
ssh azureuser@$server2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.8 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server2:/home/azureuser/result.txt qperf_results/server2_middleware2.txt
ssh azureuser@$server3 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.8 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server3:/home/azureuser/result.txt qperf_results/server3_middleware2.txt
ssh azureuser@$client1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.8 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client1:/home/azureuser/result.txt qperf_results/client1_middleware2.txt
ssh azureuser@$client2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.8 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client2:/home/azureuser/result.txt qperf_results/client2_middleware2.txt
ssh azureuser@$client3 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.8 tcp_bw tcp_lat &> result.txt"
scp azureuser@$client3:/home/azureuser/result.txt qperf_results/client3_middleware2.txt
ssh azureuser@$middleware2 "kill -9 \$(ps -aux | grep qperf | awk '{print \$2}')"

ssh azureuser@$client2 "./qperf-0.4.9/src/qperf" &
ssh azureuser@$server1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.11 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server1:/home/azureuser/result.txt qperf_results/server1_client2.txt
ssh azureuser@$server2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.11 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server2:/home/azureuser/result.txt qperf_results/server2_client2.txt
ssh azureuser@$server3 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.11 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server3:/home/azureuser/result.txt qperf_results/server3_client2.txt
ssh azureuser@$middleware1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.11 tcp_bw tcp_lat &> result.txt"
scp azureuser@$middleware1:/home/azureuser/result.txt qperf_results/middleware1_client2.txt
ssh azureuser@$middleware2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.11 tcp_bw tcp_lat &> result.txt"
scp azureuser@$middleware2:/home/azureuser/result.txt qperf_results/middleware2_client2.txt
ssh azureuser@$client2 "kill -9 \$(ps -aux | grep qperf | awk '{print \$2}')"

ssh azureuser@$client3 "./qperf-0.4.9/src/qperf" &
ssh azureuser@$server1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.10 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server1:/home/azureuser/result.txt qperf_results/server1_client3.txt
ssh azureuser@$server2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.10 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server2:/home/azureuser/result.txt qperf_results/server2_client3.txt
ssh azureuser@$server3 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.10 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server3:/home/azureuser/result.txt qperf_results/server3_client3.txt
ssh azureuser@$middleware1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.10 tcp_bw tcp_lat &> result.txt"
scp azureuser@$middleware1:/home/azureuser/result.txt qperf_results/middleware1_client3.txt
ssh azureuser@$middleware2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.10 tcp_bw tcp_lat &> result.txt"
scp azureuser@$middleware2:/home/azureuser/result.txt qperf_results/middleware2_client3.txt
ssh azureuser@$client3 "kill -9 \$(ps -aux | grep qperf | awk '{print \$2}')"


ssh azureuser@$client1 "./qperf-0.4.9/src/qperf" &
ssh azureuser@$server1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.6 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server1:/home/azureuser/result.txt qperf_results/server1_client1.txt
ssh azureuser@$server2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.6 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server2:/home/azureuser/result.txt qperf_results/server2_client1.txt
ssh azureuser@$server3 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.6 tcp_bw tcp_lat &> result.txt"
scp azureuser@$server3:/home/azureuser/result.txt qperf_results/server3_client1.txt
ssh azureuser@$middleware1 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.6 tcp_bw tcp_lat &> result.txt"
scp azureuser@$middleware1:/home/azureuser/result.txt qperf_results/middleware1_client1.txt
ssh azureuser@$middleware2 "./qperf-0.4.9/src/qperf -t 10 -v 10.0.0.6 tcp_bw tcp_lat &> result.txt"
scp azureuser@$middleware2:/home/azureuser/result.txt qperf_results/middleware2_client1.txt
ssh azureuser@$client1 "kill -9 \$(ps -aux | grep qperf | awk '{print \$2}')"

