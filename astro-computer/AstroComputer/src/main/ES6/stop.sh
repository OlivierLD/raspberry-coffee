#!/bin/bash
ps -ef | grep TinyNodeServer | grep -v grep | awk '{ print $2 }' > km
for pid in `cat km`; do
  echo Killing process ${pid}
  sudo kill -15 ${pid}
done
rm km
