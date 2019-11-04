#!/bin/bash

java -jar dist/middleware-18-904-029.jar -l localhost -p 11210 -t 1 -s false -m localhost:11211 &> mw.log &
echo $! > mw.pid
