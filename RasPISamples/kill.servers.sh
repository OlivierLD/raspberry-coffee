#!/bin/bash
#
# Use to stop the processes started by start.servers.sh
#
nocase() {
  if [ "`echo $1 | tr [:lower:] [:upper:]`" = "`echo $2 | tr [:lower:] [:upper:]`" ]
  then
    return 0  # true
  else
    return 1  # false
  fi
}
#
dokill() {
	echo "Finding process to kill for $1..."
  ps -ef | grep $1 | grep -v grep | awk '{ print $2 }' > ks
# ps -ef | grep $1 | grep -v grep | grep -v kill.servers.sh | awk '{ print $2 }' > ks
  for pid in `cat ks`
  do
    echo -n "Kill process '$1' > $pid y|[n] ? "
    read a
    if nocase "$a" "Y"
    then
      # Is sudo mandatory?
      sudo kill -15 $pid
    fi
  done
  rm ks
}
#
dokill nmea.mux.GenericNMEAMultiplexer
dokill navrest.NavServer
dokill snap.loop.sh
#
