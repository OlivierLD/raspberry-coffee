#!/bin/bash
# ps -ef | grep mux | grep -v grep | grep -v killmux | awk '{ print $2 }' > km
# ps -ef | grep GenericNMEAMultiplexer | grep -v grep | grep sudo | awk '{ print $2 }' > km
ps -ef | grep NavServer | grep -v grep | awk '{ print $2 }' > km
for pid in `cat km`; do
  echo Killing process ${pid}
  sudo kill -15 ${pid}
done
rm km
