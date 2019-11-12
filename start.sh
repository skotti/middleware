#!/bin/bash
source /home/azureuser/asl-project-2019-ruzhanskaia/environment.sh
m_address=$1
m_port=$2
m_worker_threads=$3
java -jar /home/azureuser/asl-project-2019-ruzhanskaia/dist/middleware-18-904-029.jar -l $m_address -p $m_port -t $m_worker_threads -s false -m $server1ip:11210 &>> $HOME/mw.log &
echo $! > mw.pid
