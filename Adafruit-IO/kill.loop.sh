#!/bin/bash
ps -ef | grep snap.loop | grep -v grep | awk '{ print $2 }' > ksn
for pid in $(cat ksn); do
  echo -e "Killing process ${pid}"
  sudo kill -9 ${pid}
done
rm ksn
