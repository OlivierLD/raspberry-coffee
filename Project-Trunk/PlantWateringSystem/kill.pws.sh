#!/bin/bash
# ps -ef | grep STH10 | grep -v grep | awk '{ print $2 }' > km
ps -ef | grep MCP3008 | grep -v grep | awk '{ print $2 }' > km
for pid in `cat km`
do
  echo Killing process ${pid}
  sudo kill -15 ${pid}
done
rm km
