#!/usr/bin/env bash
# Stop the TCP Watch
#
# ID=$(ps -ef | grep TCPWatch | grep -v grep | awk '{ print $2 }')
# sudo kill -9 $ID
#
ps -ef | grep TCPWatch | grep -v grep | awk '{ print $2 }' > kw
for pid in `cat kw`
do
  echo Killing process $pid
  sudo kill -15 $pid
done
rm kw
