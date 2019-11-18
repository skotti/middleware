#!/bin/bash
sudo apt install build-essential libevent-dev 
sudo update-locale LC_ALL=en_US.UTF-8 LANG=en_US.UTF-8
wget http://www.memcached.org/files/memcached-1.4.25.tar.gz
tar -zxvf memcached-1.4.25.tar.gz
cd memcached-1.4.25/
mkdir install
./configure --prefix=$HOME/memcached/memcached-1.4.25/install && make && make test && make install  
