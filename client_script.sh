#!/bin/bash
sudo apt install libevent-dev git unzip ant openjdk-8-jdk build-essential autoconf automake libpcre3-dev libevent-dev pkg-config zlib1g-dev
sudo update-locale LC_ALL=en_US.UTF-8 LANG=en_US.UTF-8
git clone https://github.com/RedisLabs/memtier_benchmark.git
cd memtier_benchmark
autoreconf -ivf && ./configure --prefix=/home/azureuser/memtier_benchmark/install --disable-tls && make && sudo make install
