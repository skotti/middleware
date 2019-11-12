#!/bin/bash
source environment.sh
m_address=$1
m_port=$2
m_worker_threads=$3
java -jar dist/middleware-18-904-029.jar -l $m_address -p $m_port -t $m_worker_threads -s false -m $server1:11210 &>> mw.log &
echo $! > mw.pid
