#!/bin/bash
dokill() {
  ps -ef | grep $1 | grep -v grep | grep -v kill.servers.sh | awk '{ print $2 }' > km
  for pid in `cat km`
  do
    echo Killing process $pid
    sudo kill -15 $pid
  done
  rm km 
}
#
dokill nmea.mux.GenericNMEAMultiplexer
dokill navrest.NavServer
dokill snap.loop.sh
#

